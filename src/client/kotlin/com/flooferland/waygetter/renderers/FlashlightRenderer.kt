package com.flooferland.waygetter.renderers

import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.models.FlashlightModel
import com.flooferland.waygetter.renderers.layers.TransparentLayer
import software.bernie.geckolib.renderer.GeoItemRenderer

class FlashlightRenderer : GeoItemRenderer<FlashlightItem>(FlashlightModel()) {
    init {
        addRenderLayer(TransparentLayer(this))
    }
}