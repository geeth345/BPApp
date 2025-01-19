package com.example.bloodpressuremonitorconnector.utils.models

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.coroutineScope

class ModelsManager (
    private val context: Context,
) {

    private var estimator: PyObject? = null
    private var risk_estimator: PyObject? = null

    init {
        intialisePython()
    }

    private fun intialisePython() {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
            }
        } catch (e: Exception) {
            Log.e("ModelsManager", "Error initialising Python: $e")
        }
        val py = Python.getInstance()

        // load the python file
        val module = py.getModule("estimator")

        // create a bp estimator object
        estimator = module.callAttr("BPEstimator")

        // create a risk estimator object
        risk_estimator = module.callAttr("CVDRiskEstimator")
    }

    fun predictBP(sample: List<Float>): BPResult {
        if (estimator == null) {
            Log.e("ModelsManager", "Estimator not initialised")
            return BPResult(0, 0)
        }
        val result = estimator?.callAttr("predict", sample)
        if (result == null) {
            Log.e("ModelsManager", "Error predicting BP")
            return BPResult(0, 0)
        }

        // Explicitly convert PyObject array elements to Int
        val systolic = result.asList()[0].toJava(Int::class.java)
        val diastolic = result.asList()[1].toJava(Int::class.java)

        return BPResult(systolic, diastolic)
    }

    fun predictRisk(sample_systolic: List<Int>, sample_diastolic: List<Int>): Int {
        if (risk_estimator == null) {
            Log.e("ModelsManager", "Risk Estimator not initialised")
            return 0
        }

        // convert to arrays first
        val sample_systolic_arr = sample_systolic.toIntArray()
        val sample_diastolic_arr = sample_diastolic.toIntArray()

        val result = risk_estimator?.callAttr("predict", sample_systolic_arr, sample_diastolic_arr)


        if (result == null) {
            Log.e("ModelsManager", "Error predicting risk")
            return 0
        }

        // convert pyobjec to java float
        val risk: Float = result.toJava(Float::class.java)

        // convert to int and return
        return risk.toInt()
    }

    fun cleanup() {
        estimator = null
    }

}

data class BPResult (
    val systolic: Int,
    val diastolic: Int
)