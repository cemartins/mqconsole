package net.sf.juffrou.mq.ui;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;
import net.sf.juffrou.mq.error.FXMLLoadingError;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *
 * @author cemartins
 */
@Component
public abstract class FXMLView implements ApplicationContextAware {

    public static final String DEFAULT_ENDING = "view";
    protected FXMLLoader loader = null;
    
    private ApplicationContext applicationContext;

    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	this.applicationContext = applicationContext;
   }

    private void init(Class clazz, String conventionalName) {
        final URL resource = clazz.getResource(conventionalName);
        this.loader = new FXMLLoader(resource);
        this.loader.setControllerFactory(new Callback<Class<?>, Object>() {
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
        });
        try {
            loader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load " + conventionalName, ex);
        }
    }

    public Parent getView() {

    	init(getClass(), getFXMLName());

        Parent parent = this.loader.getRoot();
        addCSSIfAvailable(parent);
        return parent;
    }

    void addCSSIfAvailable(Parent parent) {
        URL uri = getClass().getResource(getStyleSheetName());
        if (uri == null) {
            return;
        }
        String uriToCss = uri.toExternalForm();
        parent.getStylesheets().add(uriToCss);
    }

    String getStyleSheetName() {
        return getConventionalName(".css");
    }

    public Object getPresenter() {
    	if(loader == null)
    		init(getClass(), getFXMLName());

    	Object controller = this.loader.getController();
        return controller;
    }

    String getConventionalName(String ending) {
        String clazz = this.getClass().getSimpleName().toLowerCase();
        return stripEnding(clazz) + ending;
    }

    static String stripEnding(String clazz) {
        if (!clazz.endsWith(DEFAULT_ENDING)) {
            return clazz;
        }
        int viewIndex = clazz.lastIndexOf(DEFAULT_ENDING);
        return clazz.substring(0, viewIndex);
    }

    final String getFXMLName() {
        return getConventionalName(".fxml");
    }
}
