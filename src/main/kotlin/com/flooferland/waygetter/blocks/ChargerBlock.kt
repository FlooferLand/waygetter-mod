package com.flooferland.waygetter.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes

class ChargerBlock(properties: Properties) : HorizontalDirectionalBlock(properties) {
    val shape = Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.1875, 0.9375)!!
    override fun codec() = simpleCodec(::ChargerBlock)!!

    init {
        registerDefaultState(
            stateDefinition.any().setValue(FACING, Direction.NORTH)
        )
    }

    override fun getOcclusionShape(state: BlockState?, level: BlockGetter?, pos: BlockPos?) = Shapes.empty()!!
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext?) = shape
    override fun getCollisionShape(state: BlockState?, level: BlockGetter?, pos: BlockPos?, context: CollisionContext?) = shape

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }
}