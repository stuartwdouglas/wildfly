package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedItem;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.components.Cod;
import org.jboss.webbeans.test.components.FishFarm;
import org.jboss.webbeans.test.components.Salmon;
import org.jboss.webbeans.test.components.ScottishFish;
import org.jboss.webbeans.test.components.Sole;
import org.testng.annotations.Test;

public class InstantiationByNameTest extends AbstractTest
{
   
   @Test(expectedExceptions=AmbiguousDependencyException.class)
   public void testAmbiguousDependencies() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> plaiceBean = new BeanImpl<Cod>(new SimpleComponentModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleComponentModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleComponentModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      manager.addBean(plaiceBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      
      manager.getInstanceByName("whitefish");
   }
   
   @Test(expectedExceptions=UnsatisfiedDependencyException.class)
   public void testUnsatisfiedDependencies() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleComponentModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleComponentModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      
      manager.getInstanceByName("foo");
   }
   
}
