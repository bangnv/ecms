/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cms.jcrext.activity;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen The Vinh From ECM Of eXoPlatform
 *          vinh_nguyen@exoplatform.com
 * List of available activity event
 * List of node-type with allow the activity raise on.
 * 15 Jan 2013  
 */
public class ActivityCommonService {
  private String acceptedNodeTypes;
  private String acceptedProperties;// = "{exo:summary}{exo:title}{exo:text}";
  private String acceptedFileProperties;
  public static String NT_FILE                       = "nt:file";
  public static String WEB_CONTENT                   = "exo:webContent";
  public static String ACCESSIBLE_MEDIA              = "exo:accessibleMedia";
  public static String EDIT_ACTIVITY                 = "ActivityNotify.event.PropertyUpdated";
  public static String FILE_EDIT_ACTIVITY            = "FileActivityNotify.event.PropertyUpdated";
  public static String FILE_PROPERTY_REMOVE_ACTIVITY = "FileActivityNotify.event.PropertyRemoved";
  public static String FILE_ADD_ACTIVITY             = "FileActivityNotify.event.PropertyAdded";
  public static String FILE_REMOVE_ACTIVITY          = "FileActivityNotify.event.FileRemoved";
  public static String FILE_CREATED_ACTIVITY         = "ActivityNotify.event.FileCreated";
  
  
  public static String ATTACH_ADDED_ACTIVITY         = "ActivityNotify.event.AttachmentAdded";
  public static String ATTACH_REMOVED_ACTIVITY       = "ActivityNotify.event.AttachmentRemoved";
  
  public static String NODE_CREATED_ACTIVITY      = "ActivityNotify.event.NodeCreated";
  public static String NODE_REMOVED_ACTIVITY      = "ActivityNotify.event.NodeRemoved";
  public static String NODE_MOVED_ACTIVITY        = "ActivityNotify.event.NodeMoved";
  public static String NODE_REVISION_CHANGED      = "ActivityNotify.event.RevisionChanged";  
  
  public static String CATEGORY_ADDED_ACTIVITY    = "ActivityNotify.event.CategoryAdded";
  public static String CATEGORY_REMOVED_ACTIVITY  = "ActivityNotify.event.CategoryRemoved";
  
  public static String TAG_ADDED_ACTIVITY         = "ActivityNotify.event.TagAdded";
  public static String TAG_REMOVED_ACTIVITY       = "ActivityNotify.event.TagRemoved";
  
  public static String COMMENT_ADDED_ACTIVITY     = "ActivityNotify.event.CommentAdded";
  public static String COMMENT_UPDATED_ACTIVITY   = "ActivityNotify.event.CommentUpdated";
  public static String COMMENT_REMOVED_ACTIVITY   = "ActivityNotify.event.CommentRemoved";
  
  public static String STATE_CHANGED_ACTIVITY     = "ActivityNotify.event.StateChanged";
  
  public static String VALUE_SEPERATOR            = "_S#E#P#R_";
  
  public static String METADATA_VALUE_SEPERATOR   = "_M#S#E#P#R_";
  
  public static String MIX_COMMENT                = "exo:activityComment";
  public static String MIX_COMMENT_CREATOR        = "exo:activityCreator";
  public static String MIX_COMMENT_CREATING       = "exo:activityCreating";
  public static String MIX_COMMENT_ACTIVITY_ID    = "exo:activityCommentID";
  
  private Map<String, Object> properties = new HashMap<String, Object>();
  
  public Map<String, Object> getPreProperties() { return properties; }
  
  public void setPreProperties(Map<String, Object> preProperties) { properties = preProperties; }
  
  public ActivityCommonService(InitParams initParams) {
    this.acceptedNodeTypes = initParams.getValueParam("acceptedNodeTypes").getValue();
    this.acceptedProperties = initParams.getValueParam("acceptedProperties").getValue();
    this.acceptedFileProperties = initParams.getValueParam("acceptedFileProperties").getValue();
    if (acceptedNodeTypes==null) acceptedNodeTypes = "";
    if (acceptedProperties==null) acceptedProperties =""; 
    if (acceptedFileProperties==null) acceptedFileProperties ="";
  }
  
  public boolean isCreating(Node node) {
    String isCreating;
    try {
      if (node.isNodeType(MIX_COMMENT)) {
        if (node.hasProperty(MIX_COMMENT_CREATING)) {
          isCreating = node.getProperty(MIX_COMMENT_CREATING).getString();
          return isCreating.equalsIgnoreCase("true");
        }
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }
  public boolean isAcceptedNode(Node node) {
    try {
      return node==null?false:acceptedNodeTypes.indexOf("{" + node.getPrimaryNodeType().getName() +"}")>=0;
    } catch (RepositoryException e) {
      return false;
    }
  }
  private boolean isDocumentNodeType(Node node) throws Exception {
  	boolean isBroadCast = true;
    TemplateService templateService = WCMCoreUtils.getService(TemplateService.class);
    isBroadCast = templateService.getAllDocumentNodeTypes().contains(node.getPrimaryNodeType().getName());
    
    
    
    if(!isBroadCast) {
    	isBroadCast = !(node.isNodeType(NodetypeConstant.NT_UNSTRUCTURED) || node.isNodeType(NodetypeConstant.NT_FOLDER));
    }
    return isBroadCast;
  }
  
  public boolean isBroadcastNTFileEvents(Node node) throws Exception {
  	boolean result = true;
  	while(result && !((NodeImpl)node).isRoot()) {
  		node = node.getParent();
  		result = !isDocumentNodeType(node);
  	}
  	return result;
  }
  public boolean isAcceptedProperties(String propertyName) {
    return (acceptedProperties.indexOf("{" + propertyName + "}")>=0);
  }
  public boolean isAcceptedFileProperties(String propertyName) {
    return (acceptedFileProperties.indexOf("{" + propertyName + "}")>=0 || propertyName.startsWith("dc:"));  
  }
  
  public Node isSpecialContentNodeType(Item item) {
    String webSpecialContentPath = "default.html/jcr:content/jcr:data";
    String specialSummaryCase    = "jcr:content/dc:description";
    String path;
    try {
      path = item.getPath();
    } catch (RepositoryException e1) {
      return null;
    }
    if ( path.endsWith(webSpecialContentPath)) {
      try {
        Node node = (Node) item.getParent().getParent().getParent();
        if (node.isNodeType(WEB_CONTENT)) return node;
      }catch (Exception e) {
        return null;
      }
    }else if (path.endsWith(specialSummaryCase)){
      try {
        Node node = (Node) item.getParent().getParent();
        if (isAcceptedNode(node)) return node;
      }catch (Exception e) {
        return null;
      }
    }
    return null;
  }
}