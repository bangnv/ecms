/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.admin.views;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.cms.views.ManageViewService;
import org.exoplatform.services.cms.views.ViewConfig.Tab;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jun 28, 2006
 */

@ComponentConfig(
        template = "app:/groovy/webui/component/admin/view/UITabForm.gtmpl",
        lifecycle = UIFormLifecycle.class,
        events = {    
          @EventConfig(listeners = UITabForm.SaveActionListener.class),
          @EventConfig(listeners = UITabForm.CancelActionListener.class, phase = Phase.DECODE)
      }
)
public class UITabForm extends UIForm {

  final static public String FIELD_NAME = "tabName" ;
  private List<?> buttons_ ;
  
  public UITabForm() throws Exception {
    setComponentConfig(getClass(), null) ;
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
    ManageViewService vservice_ = getApplicationComponent(ManageViewService.class) ;
    buttons_ = vservice_.getButtons();
    for(Object bt : buttons_) {
      addUIFormInput(new UICheckBoxInput(getButtonName(bt), "", null)) ;
    }
  }

  private String getButtonName(Object bt) {
    String button = (String) bt;
    return button.substring(0, 1).toLowerCase() + button.substring(1);
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context) ;
  }

  public void refresh(boolean isEditable) throws Exception {
    getUIStringInput(FIELD_NAME).setDisabled(!isEditable).setValue(null) ;
    for(Object bt : buttons_){
      getUICheckBoxInput(getButtonName(bt)).setChecked(false).setDisabled(!isEditable) ;
    }
  }

  public void update(Tab tab, boolean isView) throws Exception{
    refresh(!isView) ;
    if(tab == null) return ;
    getUIStringInput(FIELD_NAME).setDisabled(true).setValue(tab.getTabName()) ;
    String buttonsProperty = tab.getButtons() ;
    String[] buttonArray = StringUtils.split(buttonsProperty, ";") ;
    for(String bt : buttonArray){
      UICheckBoxInput cbInput = getUICheckBoxInput(bt.trim()) ;
      if(cbInput != null) cbInput.setChecked(true) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UITabForm> {
    public void execute(Event<UITabForm> event) throws Exception {
      UITabForm uiTabForm = event.getSource();
      UIViewContainer uiContainer = uiTabForm.getAncestorOfType(UIViewContainer.class);
      UIViewFormTabPane viewFormTabPane = uiContainer.findFirstComponentOfType(UIViewFormTabPane.class);
      String tabName = uiTabForm.getUIStringInput(FIELD_NAME).getValue() ;
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class) ;
      if(tabName == null || tabName.trim().length() == 0) {
        viewFormTabPane.setSelectedTab(uiTabForm.getId()) ;
        uiApp.addMessage(new ApplicationMessage("UITabForm.msg.tab-name-error", null, ApplicationMessage.WARNING)) ;
        
        return ;
      }
      String[] arrFilterChar = {"&", "$", "@", ",", ":","]", "[", "*", "%", "!"} ;
      for(String filterChar : arrFilterChar) {
        if(tabName.indexOf(filterChar) > -1) {
          uiApp.addMessage(new ApplicationMessage("UITabForm.msg.fileName-invalid", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiTabForm);
          return ;
        }
      }
      StringBuilder selectedButton = new StringBuilder() ;
      boolean isSelected = false ;
      for(Object bt : uiTabForm.buttons_ ) {
        String button = uiTabForm.getButtonName(bt) ;
        if(uiTabForm.getUICheckBoxInput(button).isChecked()) {
          isSelected = true ;
          if(selectedButton.length() > 0) selectedButton.append(";").append(button) ;
          else selectedButton.append(button) ;
        }
      }
      if(!isSelected) {
        viewFormTabPane.setSelectedTab(uiTabForm.getId());
        uiApp.addMessage(new ApplicationMessage("UITabForm.msg.button-select-error", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTabForm);
        return;
      }
      UIViewForm uiViewForm = viewFormTabPane.getChild(UIViewForm.class);
      uiViewForm.addTab(tabName, selectedButton.toString());
      UIPopupWindow uiPopup = uiContainer.getChildById(UITabList.TAPFORM_POPUP);
      uiPopup.setShow(false);
      uiPopup.setRendered(false);
      viewFormTabPane.setSelectedTab(viewFormTabPane.getSelectedTabId()) ;
      UITabList uiTabList = viewFormTabPane.getChild(UITabList.class);
      uiTabList.refresh(uiTabList.getUIPageIterator().getCurrentPage());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }
  
  static public class CancelActionListener extends EventListener<UITabForm> {
    public void execute(Event<UITabForm> event) throws Exception {
      UITabForm uiTabForm = event.getSource();
      UIViewContainer uiContainer = uiTabForm.getAncestorOfType(UIViewContainer.class);
      UIViewFormTabPane uiTabPane = uiContainer.findFirstComponentOfType(UIViewFormTabPane.class);
      UIPopupWindow uiPopup = uiContainer.getChildById(UITabList.TAPFORM_POPUP);
      uiPopup.setShow(false);
      uiPopup.setRendered(false);
      uiTabPane.setSelectedTab(uiTabPane.getChild(UITabList.class).getId());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }

}
