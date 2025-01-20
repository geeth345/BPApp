import numpy as np
from scipy.signal import butter, filtfilt, find_peaks
import pickle
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import GradientBoostingRegressor

class SignalProcessor:
    def __init__(self, fs=125):
        self.fs = fs

    def bandpass_filter(self, data, lowcut=0.5, highcut=3.0, order=4):
        """
        Applies a Butterworth bandpass filter to the signal.
        Args:
            data: List of float values representing the signal
            lowcut: Lower cutoff frequency (Hz)
            highcut: Upper cutoff frequency (Hz)
            order: Filter order
        Returns:
            Filtered signal as numpy array
        """
        nyquist = 0.5 * self.fs
        low = lowcut / nyquist
        high = highcut / nyquist
        b, a = butter(order, [low, high], btype='band')
        filtered_data = filtfilt(b, a, data)
        return filtered_data

    def extract_features(self, signal_data):
        """
        Extracts features from filtered signal data
        Args:
            signal_data: Numpy array of filtered signal values
        Returns:
            Dictionary of extracted features
        """
        # Find systolic points
        systolic_peaks, _ = find_peaks(signal_data, prominence=0.01, distance=int(0.25 * self.fs))

        # Find diastolic points
        diastolic_points, _ = find_peaks(-signal_data, prominence=0.05, distance=int(0.5 * self.fs))

        features_list = []

        for i in range(len(systolic_peaks)-1):
            curr_systolic = systolic_peaks[i]
            next_systolic = systolic_peaks[i+1]

            # Find surrounding diastolic points
            prev_diastolic = diastolic_points[diastolic_points < curr_systolic]
            next_diastolic = diastolic_points[diastolic_points > curr_systolic]

            if len(prev_diastolic) == 0 or len(next_diastolic) == 0:
                continue

            diastolic_point1 = prev_diastolic[-1]
            diastolic_point2 = next_diastolic[0]

            # Find dicrotic notch and peak
            pulse_segment = signal_data[curr_systolic:diastolic_point2]
            notch_idx, _ = find_peaks(-pulse_segment, distance=int(0.1*self.fs))

            if len(notch_idx) == 0:
                continue

            dicrotic_notch = notch_idx[0] + curr_systolic

            post_notch_segment = signal_data[dicrotic_notch:diastolic_point2]
            dicrotic_peaks, _ = find_peaks(post_notch_segment, distance=int(0.1*self.fs))

            if len(dicrotic_peaks) == 0:
                continue

            dicrotic_peak = dicrotic_peaks[0] + dicrotic_notch

            # Extract features
            features = {
                'heart_rate': 60 / ((next_systolic - curr_systolic) / self.fs),
                'systolic_peak': signal_data[curr_systolic] * 1000,
                'dicrotic_peak': signal_data[dicrotic_peak] * 1000,
                'diastolic_point1': signal_data[diastolic_point1] * 1000,
                'diastolic_point2': signal_data[diastolic_point2] * 1000,
                'dicrotic_notch': signal_data[dicrotic_notch] * 1000,
                'max_slope': np.max(np.diff(signal_data[diastolic_point1:curr_systolic])) * self.fs,
                'augmentation_index': signal_data[curr_systolic] / signal_data[dicrotic_peak],
                'T1': (curr_systolic - diastolic_point1) / self.fs * 1000,
                'T2': (dicrotic_peak - curr_systolic) / self.fs * 1000,
                'T3': (diastolic_point2 - dicrotic_peak) / self.fs * 1000
            }
            features_list.append(features)

        return features_list

class BPEstimator:
    def __init__(self, model_path=None):
        """
        Initialize BP estimator with optional pre-trained model
        Args:
            model_path: Path to saved model file (if None, creates new model)
        """
        if model_path:
            with open(model_path, 'rb') as f:
                saved_data = pickle.load(f)
                self.scaler = saved_data['scaler']
                self.sbp_model = saved_data['sbp_model']
                self.dbp_model = saved_data['dbp_model']
        else:
            self.scaler = StandardScaler()
            self.sbp_model = GradientBoostingRegressor(n_estimators=100, learning_rate=0.1, max_depth=3, random_state=42)
            self.dbp_model = GradientBoostingRegressor(n_estimators=100, learning_rate=0.1, max_depth=3, random_state=42)

        self.processor = SignalProcessor()

    def predict(self, sample_list):
        """
        Predict BP values from extracted features
        Args:
            features: Dictionary of features from SignalProcessor
        Returns:
            Tuple of (systolic_bp, diastolic_bp)
        """


        feature_names = ['heart_rate', 'systolic_peak', 'dicrotic_peak',
                        'diastolic_point1', 'diastolic_point2', 'dicrotic_notch',
                        'max_slope', 'augmentation_index', 'T1', 'T2', 'T3']

        X = np.array([[features[name] for name in feature_names]])
        X_scaled = self.scaler.transform(X)

        sbp = self.sbp_model.predict(X_scaled)[0]
        dbp = self.dbp_model.predict(X_scaled)[0]

        return sbp, dbp

    def process_signal(self, raw_signal, fs=200):
        """
        Main function to process raw signal and estimate blood pressure
        Args:
            raw_signal: List of float values from sensor
            fs: Sampling frequency (Hz)
        Returns:
            Tuple of (systolic_bp, diastolic_bp) or None if processing fails
        """
        try:
            signal = np.array(raw_signal)
            filtered_signal = self.processor.bandpass_filter(signal)
            features_list = self.processor.extract_features(filtered_signal)

            if not features_list:
                return None

            # Use first complete pulse for estimation
            # TODO: Could using multiple and then averaging improve performance?
            features = features_list[0]

            # Load pre-trained model and estimate BP
            estimator = BPEstimator("model.pkl")  # You'll need to provide the trained model
            sbp, dbp = estimator.predict(features)

            return sbp, dbp

        except Exception as e:
            print(f"Error processing signal: {str(e)}")
            return (0, 0)


class CVDRiskEstimator:
    def __init__(self):
        # Initialise the risk estimation model
        self.risk_model = None

    def predict(self, samples_systolic, samples_diastolic):
        # Predict the risk of cardiovascular disease given a list of blood pressure measurements
        # collected over the course of a month
        # takes two lists of ints
        
        # Convert Java List to Python list using the tolist() method
        systolic_list = list(samples_systolic)
        diastolic_list = list(samples_diastolic)

        # Placeholder measurements
        avg_systolic = sum(systolic_list) / len(systolic_list)
        avg_diastolic = sum(diastolic_list) / len(diastolic_list)

        # Calculate systolic risk
        if avg_systolic < 120:
            systolic_risk = 0
        elif avg_systolic < 130:
            systolic_risk = 20
        elif avg_systolic < 140:
            systolic_risk = 40
        elif avg_systolic < 160:
            systolic_risk = 60
        elif avg_systolic < 180:
            systolic_risk = 80
        else:
            systolic_risk = 100

        # Calculate diastolic risk
        if avg_diastolic < 80:
            diastolic_risk = 0
        elif avg_diastolic < 85:
            diastolic_risk = 20
        elif avg_diastolic < 90:
            diastolic_risk = 40
        elif avg_diastolic < 100:
            diastolic_risk = 60
        elif avg_diastolic < 110:
            diastolic_risk = 80
        else:
            diastolic_risk = 100

        # Return average risk
        return (systolic_risk + diastolic_risk) / 2

