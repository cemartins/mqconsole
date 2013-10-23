package net.sf.juffrou.mq.ui;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import net.sf.juffrou.mq.error.FXMLLoadingError;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

public class SpringFxmlLoader extends FXMLLoader {

	private final ApplicationContext applicationContext;

	public SpringFxmlLoader(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "ApplicationContext cannot be null");
		this.applicationContext = applicationContext;

		setControllerFactory(new StringFxmlControllerFactory());
	}

	public Object load(String url) {
		try (InputStream fxmlStream = SpringFxmlLoader.class.getResourceAsStream(url)) {
			return super.load(fxmlStream);
		} catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private class StringFxmlControllerFactory implements Callback<Class<?>, Object> {

		@Override
		public Object call(Class<?> controllerClass) {
			Object bean = null;
			try {

				bean = applicationContext.getBean(controllerClass);
			} catch (NoSuchBeanDefinitionException e) {
				try {
					bean = controllerClass.newInstance();
				} catch (InstantiationException e1) {
					throw new FXMLLoadingError("Cannot load controller " + controllerClass.getName(), e);
				} catch (IllegalAccessException e1) {
					throw new FXMLLoadingError("Cannot load controller " + controllerClass.getName(), e);
				}
			}
			return bean;
		}

	}
}
