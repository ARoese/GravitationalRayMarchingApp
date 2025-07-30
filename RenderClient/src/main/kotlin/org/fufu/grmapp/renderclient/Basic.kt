package org.fufu.grmapp.renderclient

import protokt.v1.grm.protobuf.Body
import protokt.v1.grm.protobuf.Camera
import protokt.v1.grm.protobuf.Float2
import protokt.v1.grm.protobuf.Float3
import protokt.v1.grm.protobuf.Material
import protokt.v1.grm.protobuf.RenderConfig
import protokt.v1.grm.protobuf.RenderDevice
import protokt.v1.grm.protobuf.RenderRequest
import protokt.v1.grm.protobuf.Scene
import protokt.v1.grm.protobuf.UInt2
import protokt.v1.grm.protobuf.UInt3
import protokt.v1.grm.protobuf.UniversalConstants
import kotlin.math.PI

const val DEG2RAD: Float = (PI / 180f).toFloat()

fun make_test_scene(): Scene{
    return Scene {
        nohit = Material {
            shader = Material.Shader.Color(UInt3{
                x = 255.toUInt()
                y = 0.toUInt()
                z = 255.toUInt()
            })
        }
        cam = Camera {
            fov = Float2 {
                x = 50f*DEG2RAD
                y = 50f*DEG2RAD
            }
            camPos = Float3 {
                x = 0f
                y = 0f
                z = 0f
            }
            camRot = Float3 {
                x = 0f
                y = 0f
                z = 0f
            }
        }
        constants = UniversalConstants {
            g = 6.6743e-11f
            c = 10f
        }
        bodies = listOf(
            //sun
            Body {
                radius = 20f
                mass = 0f
                position = Float3 {
                    x = 250f
                    y = -60f
                    z = 0f
                }
                material = Material {
                    shader = Material.Shader.Color(UInt3{
                        x = 255.toUInt()
                        y = 255.toUInt()
                        z = 0.toUInt()
                    })
                }
            },
            // black hole
            Body {
                radius = 6f
                mass = 1e11f
                position = Float3 {
                    x = 250f
                    y = 0f
                    z = 0f
                }
                material = Material {
                    shader = Material.Shader.Color(UInt3{
                        x = 0.toUInt()
                        y = 0.toUInt()
                        z = 0.toUInt()
                    })
                }
            }
        )
    }
}

suspend fun simpleTestRender(renderServer: RenderServer): ResponseTexture {
    val test_height = 32.toUInt()
    val test_width = 32.toUInt()
    val renderRequest = RenderRequest{
        this.scene = make_test_scene()
        this.config = RenderConfig{
            resolution = UInt2 {
                x = test_width
                y = test_height
            }
        }
        this.device = RenderDevice.CPU
    }
    return renderServer.render(renderRequest, emptyMap())
}