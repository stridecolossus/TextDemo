package org.sarge.jove.demo.text;

import java.io.IOException;

import org.sarge.jove.io.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

@Configuration
public class TextureConfiguration {
	@Autowired private LogicalDevice dev;

	@Bean
	Sampler sampler() {
		return new Sampler.Builder()
				.anisotropy(8)
				.build(dev);
	}

	@Bean
	View texture(Command.Pool graphics, Allocator allocator) throws IOException {
		// Load texture image
		final var loader = new ResourceLoaderAdapter<>(new ClasspathDataSource(), new NativeImageLoader());
		final ImageData image = loader.load("DemoFont.png");

		// Determine image format
		final VkFormat format = FormatBuilder.format(image.layout());
//		final VkFormat format = VkFormat.R8G8B8A8_UNORM;
//System.err.println("IMAGE="+format);

		// Create descriptor
		final Descriptor descriptor = new Descriptor.Builder()
				.type(VkImageType.TWO_D)
				.aspect(VkImageAspect.COLOR)
				.extents(image.size())
				.format(format)
				.build();

		// Init image memory properties
		final var props = new MemoryProperties.Builder<VkImageUsageFlag>()
				.usage(VkImageUsageFlag.TRANSFER_DST)
				.usage(VkImageUsageFlag.SAMPLED)
				.required(VkMemoryProperty.DEVICE_LOCAL)
				.build();

		// Create texture
		final Image texture = new DefaultImage.Builder()
				.descriptor(descriptor)
				.properties(props)
				.build(dev, allocator);

		// Prepare texture
		new Barrier.Builder()
				.source(VkPipelineStage.TOP_OF_PIPE)
				.destination(VkPipelineStage.TRANSFER)
				.image(texture)
					.newLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.destination(VkAccess.TRANSFER_WRITE)
					.build()
				.build()
				.submit(graphics);

		// Create staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, image.data());

		// Copy staging to texture
		new ImageTransferCommand.Builder()
				.buffer(staging)
				.image(texture)
				.layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
				.region(image)
				.build()
				.submit(graphics);

		// Release staging
		staging.destroy();

		// Transition to sampled image
		new Barrier.Builder()
			.source(VkPipelineStage.TRANSFER)
			.destination(VkPipelineStage.FRAGMENT_SHADER)
			.image(texture)
				.oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
				.newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL)
				.source(VkAccess.TRANSFER_WRITE)
				.destination(VkAccess.SHADER_READ)
				.build()
			.build()
			.submit(graphics);

		// Create texture view
		return new View.Builder(texture)
				.mapping(ComponentMapping.of(image.channels()))
				.build(dev);
	}
}
