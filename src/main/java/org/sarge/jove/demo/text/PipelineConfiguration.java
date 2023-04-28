package org.sarge.jove.demo.text;

import java.io.*;

import org.sarge.jove.common.*;
import org.sarge.jove.io.*;
import org.sarge.jove.model.Mesh;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.render.*;
import org.springframework.context.annotation.*;

@Configuration
class PipelineConfiguration {
	private final LogicalDevice dev;
	private final ResourceLoaderAdapter<InputStream, Shader> loader;

	PipelineConfiguration(LogicalDevice dev, DataSource classpath) {
		this.dev = dev;
		this.loader = new ResourceLoaderAdapter<>(classpath, new Shader.Loader(dev));
	}

	@Bean
	Shader vertex() throws IOException {
		return loader.load("text.vert.spiv");
	}

	@Bean
	Shader fragment() throws IOException {
		return loader.load("text.frag.spiv");
	}

	@Bean
	PipelineLayout pipelineLayout(DescriptorSet.Layout layout) {
		return new PipelineLayout.Builder()
				.add(layout)
				.build(dev);
	}

	@Bean
	public Pipeline pipeline(RenderPass pass, Shader vertex, Shader fragment, PipelineLayout layout, Mesh mesh) {
		return new GraphicsPipelineBuilder(pass)
				.viewport(new Rectangle(new Dimensions(512, 512)))
				.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, vertex))
				.shader(new ProgrammableShaderStage(VkShaderStage.FRAGMENT, fragment))
				.input()
					.add(mesh.layout())
					.build()
				.assembly()
					.topology(mesh.primitive())
					.build()
				.blend()
					.attachment()
						.build()
					.build()
				.build(dev, layout);
	}
}
