package org.sarge.jove.demo.text;

import org.sarge.jove.common.*;
import org.sarge.jove.control.WindowListener;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.vulkan.core.Instance;
import org.sarge.jove.scene.core.RenderLoop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

@Configuration
class DesktopConfiguration {
	@Bean
	public static Desktop desktop() {
		final Desktop desktop = Desktop.create();
		if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");
		return desktop;
	}

	@Bean
	public static Window window(Desktop desktop) {
		return new Window.Builder()
				.title("Text Demo")
				.size(new Dimensions(512, 512))
				.hint(Window.Hint.RESIZABLE, false)
				.hint(Window.Hint.CLIENT_API, 0)
				.build(desktop);
	}

	@Bean("surface-handle")
	public static Handle surface(Instance instance, Window window) {
		return window.surface(instance.handle());
	}

	@Autowired
	void close(Window window) {
		window.listener(WindowListener.Type.CLOSED, (type, state) -> System.exit(0));
	}

	@Autowired
	void pause(RenderLoop loop, Window window) {
		final WindowListener minimised = (__, state) -> {
			if(state) {
				loop.pause();
			}
			else {
				loop.restart();
			}
		};
		window.listener(WindowListener.Type.ICONIFIED, minimised);
	}
}
