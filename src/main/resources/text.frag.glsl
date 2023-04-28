#version 450 core

layout(binding = 0) uniform sampler2D font;

layout(location = 0) in vec2 inCoord;

layout(location = 0) out vec4 outColour;

void main(void) {
    outColour = texture(font, inCoord);
}
