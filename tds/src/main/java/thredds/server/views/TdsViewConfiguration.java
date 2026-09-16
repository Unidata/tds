/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.views;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.BeanNameViewResolver;

@Configuration
public class TdsViewConfiguration {

  @Bean("threddsFileView")
  public View threddsFileView() {
    return new FileView();
  }

  @Bean("threddsInvCatXmlView")
  public View threddsInvCatXmlView() {
    return new InvCatalogXmlView();
  }

  @Bean("threddsXmlView")
  public View threddsXmlView() {
    return new XmlView();
  }

  @Bean("threddsXsltView")
  public View threddsXsltView() {
    return new XsltForHtmlView();
  }

  @Bean
  public BeanNameViewResolver threddsBeanNameViewResolver() {
    BeanNameViewResolver r = new BeanNameViewResolver();
    r.setOrder(1); // same slot XmlViewResolver occupied; before Thymeleaf (2) and JSP (9)
    return r;
  }
}
