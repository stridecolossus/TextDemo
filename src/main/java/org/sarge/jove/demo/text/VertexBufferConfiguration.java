package org.sarge.jove.demo.text;

import org.sarge.jove.io.*;
import org.sarge.jove.model.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.*;
import org.springframework.context.annotation.*;

@Configuration
public class VertexBufferConfiguration {
	@Bean
	static GlyphFont font(DataSource classpath) {
		final var loader = new ResourceLoaderAdapter<>(classpath, new GlyphFont.Loader());
		return loader.load("DemoFont.yaml");
	}

	@Bean
	static Mesh mesh(GlyphFont font) {
		return new GlyphMeshBuilder(font)
				.scale(2.5f)
				.add("frog AW WA")
				//.add("Hello, world!")
				.mesh();
	}

	@Bean
	static VertexBuffer vbo(LogicalDevice dev, Allocator allocator, Mesh mesh, Command.Pool graphics) {
		// Create staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, mesh.vertices());

		// Init VBO properties
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.TRANSFER_DST)
				.usage(VkBufferUsageFlag.VERTEX_BUFFER)
				.required(VkMemoryProperty.DEVICE_LOCAL)
				.build();

		// Create destination
		final VulkanBuffer buffer = VulkanBuffer.create(dev, allocator, staging.length(), props);

		// Copy to destination
		staging.copy(buffer).submit(graphics);
		staging.destroy();

		// Create VBO
		return new VertexBuffer(buffer);
	}
}
