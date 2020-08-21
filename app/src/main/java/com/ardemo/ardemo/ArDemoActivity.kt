package com.ardemo.ardemo

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.viro.core.*
import kotlinx.android.synthetic.main.viro_view_hud.*
import java.io.IOException
import java.io.InputStream


class ArDemoActivity : AppCompatActivity(), ARScene.Listener, View.OnClickListener {

    private var viroView: ViroView? = null
    private var arScene: ARScene? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViroView()
    }

    private fun initViroView() {
        viroView = ViroViewARCore(this, object : ViroViewARCore.StartupListener {

            override fun onSuccess() {
                displayScene()
            }

            override fun onFailure(error: ViroViewARCore.StartupError, errorMessage: String) {
                Log.e("jp", "Error initializing AR [$errorMessage]")
            }
        })
        setContentView(viroView)
        View.inflate(this, R.layout.viro_view_hud, viroView as ViewGroup)
        imvBack.setOnClickListener(this)
    }

    private fun displayScene() {
        arScene = ARScene()
        val rootNode = arScene?.rootNode
        val lightPositions = ArrayList<Vector>()
        lightPositions.add(Vector(-10f, 10f, 1f))
        lightPositions.add(Vector(10f, 10f, 1f))

        val intensity = 300f
        val lightColors = ArrayList<Int>()
        lightColors.add(Color.WHITE)
        lightColors.add(Color.WHITE)

        for (i in lightPositions.indices) {
            val light = OmniLight()
            light.color = lightColors[i].toLong()
            light.position = lightPositions[i]
            light.attenuationStartDistance = 20f
            light.attenuationEndDistance = 30f
            light.intensity = intensity
            rootNode?.addLight(light)
        }
        val environment =
            Texture.loadRadianceHDRTexture(Uri.parse("file:///android_asset/ibl_newport_loft.hdr"))
        arScene?.lightingEnvironment = environment
        viroView?.scene = arScene
        createDroidAtPosition(Vector(0f, 0f, -3f))
    }

    private fun createDroidAtPosition(position: Vector) {
//         Create a droid on the surface
        val bot: Bitmap? = getBitmapFromAsset(this, "test_Albedo.png")
        val object3D = Object3D()
        object3D.setPosition(position)
        object3D.setScale(Vector(0.01f, 0.01f, 0.01f))
        arScene?.rootNode?.addChildNode(object3D)

        // Load the Android model asynchronously.
        object3D.loadModel(
            viroView?.viroContext,
            Uri.parse("file:///android_asset/test.obj"),
            Object3D.Type.OBJ,
            object : AsyncObject3DListener {
                override fun onObject3DLoaded(objectOne: Object3D, type: Object3D.Type) {
                    // When the model is loaded, set the texture associated with this OBJ
                    val objectTexture =
                        Texture(bot, Texture.Format.RGBA8, false, false)
                    val material = Material()
                    material.diffuseTexture = objectTexture

                    // Give the material a more "metallic" appearance, so it reflects the environment map.
                    // By setting its lighting model to PHYSICALLY_BASED, we enable PBR rendering on the
                    // model.
//                    material.roughness = 0.23f
//                    material.metalness = 0.7f
//                    material.lightingModel = Material.LightingModel.PHYSICALLY_BASED
                    objectOne.geometry.materials = listOf(material)
                    AnimationTransaction.begin()
                    AnimationTransaction.setAnimationDuration(5000)
                    AnimationTransaction.setAnimationLoop(true)
                    AnimationTransaction.setTimingFunction(AnimationTimingFunction.Linear)
                    object3D.setRotation(Vector(0f, 360f, 0f))
                    AnimationTransaction.commit()
                }

                override fun onObject3DFailed(s: String) {}
            })

        // Make the object draggable.
        object3D.dragListener = DragListener { i, node, vector, vector1 ->
            // No-op.
        }
        object3D.dragType = Node.DragType.FIXED_DISTANCE

    }

    override fun onTrackingInitialized() {

    }

    override fun onTrackingUpdated(p0: ARScene.TrackingState?, p1: ARScene.TrackingStateReason?) {

    }

    override fun onAmbientLightUpdate(p0: Float, p1: Vector?) {

    }

    override fun onAnchorUpdated(p0: ARAnchor?, p1: ARNode?) {

    }

    override fun onAnchorFound(p0: ARAnchor?, p1: ARNode?) {

    }

    override fun onAnchorRemoved(p0: ARAnchor?, p1: ARNode?) {

    }

    override fun onClick(v: View?) {
        finish()
    }

    override fun onStart() {
        super.onStart()
        viroView?.onActivityStarted(this)
    }

    override fun onResume() {
        super.onResume()
        viroView?.onActivityResumed(this)
    }

    override fun onPause() {
        super.onPause()
        viroView?.onActivityPaused(this)
    }

    override fun onStop() {
        super.onStop()
        viroView?.onActivityStopped(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viroView?.onActivityDestroyed(this)
    }

    private fun getBitmapFromAsset(context: Context, assetName: String): Bitmap? {
        val assetManager: AssetManager = context.resources.assets
        val imageStream: InputStream
        imageStream = try {
            assetManager.open(assetName)
        } catch (exception: IOException) {
            Log.w(
                "jp", "Unable to find image [" + assetName + "] in assets! Error: "
                        + exception.localizedMessage
            )
            return null
        }
        return BitmapFactory.decodeStream(imageStream)
    }
}