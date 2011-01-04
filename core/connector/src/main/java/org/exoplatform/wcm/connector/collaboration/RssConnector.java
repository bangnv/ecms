/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.connector.collaboration;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.ProductVersions;
import org.exoplatform.ecm.utils.text.Text;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.wcm.connector.BaseConnector;
import org.w3c.dom.Document;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong_phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * 29-08-2009
 */

@Path("/feed/")
public class RssConnector extends BaseConnector implements ResourceContainer {

  /** The WORKSPACE. */
  static private String WORKSPACE = "workspace".intern() ;
  
  /** The REPOSITORY. */
  static private String REPOSITORY = "repository".intern() ;
  
  /** The RS s_ version. */
  static private String RSS_VERSION = "rss_2.0".intern() ;
  
  /** The FEE d_ title. */
  static private String FEED_TITLE = "exo:feedTitle".intern() ;
  
  /** The DESCRIPTION. */
  static private String DESCRIPTION = "exo:description".intern() ;
  
  /** The TITLE. */
  static private String TITLE = "exo:title";
  
  /** The LINK. */
  static private String LINK = "exo:link".intern() ;
  
  /** The SUMMARY. */
  static private String SUMMARY = "exo:summary";

  /** The detail page */
  static private String DETAIL_PAGE = "detail-page";
  
  /** The detail get param */
  static private String DETAIL_PARAM = "detail-param";
  
  /** The wcm composer service. */
  private WCMComposer wcmComposer;
  
  /** The wcm configuration service. */
  private WCMConfigurationService wcmConfigurationService;
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(RssConnector.class);
  
  /**
   * Instantiates a new rss connector.
   * 
   * @param container the container
   */
  public RssConnector() {
	  wcmConfigurationService = WCMCoreUtils.getService(WCMConfigurationService.class);
      wcmComposer = WCMCoreUtils.getService(WCMComposer.class);
  }

  /**
   * Generate.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param server the server
   * @param siteName the site name
   * @param categoryPath the category path
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  @GET
  @Path("/rss/")
//  @OutputTransformer(XMLOutputTransformer.class)
  public Response generate ( 
      @QueryParam("repository") String repositoryName, 
      @QueryParam("workspace") String workspaceName,
      @QueryParam("server") String server,
      @QueryParam("siteName") String siteName,
      @QueryParam("title") String title,
      @QueryParam("desc") String desc,
      @QueryParam("folderPath") String folderPath,
      @QueryParam("orderBy") String orderBy,
      @QueryParam("orderType") String orderType,
      @QueryParam("lang") String lang,
      @QueryParam("detailPage") String detailPage,
      @QueryParam("detailParam") String detailParam,
	  @QueryParam("recursive") String recursive
	  ) throws Exception {
    
    Map<String, String> contextRss = new HashMap<String, String>();
    contextRss.put(REPOSITORY, repositoryName);
    contextRss.put(WORKSPACE, workspaceName);
    contextRss.put(RSS_VERSION, "rss_2.0");
    contextRss.put(FEED_TITLE, title);
    if (desc==null) desc = "Powered by eXo "+ProductVersions.getCurrentVersion();
    contextRss.put(DESCRIPTION, desc);
    
    contextRss.put(LINK, server);

    if (detailPage == null) detailPage  = wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.PARAMETERIZED_PAGE_URI);
    contextRss.put(DETAIL_PAGE, detailPage);
    if (detailParam == null) detailParam  = "content-id";
    contextRss.put(DETAIL_PARAM, detailParam);
    
    
    HashMap<String, String> filters = new HashMap<String, String>();
    filters.put(WCMComposer.FILTER_MODE, WCMComposer.MODE_LIVE);
    if (orderType == null) orderType = "DESC";
    if (orderBy == null) orderBy = "publication:liveDate";
    if (lang == null) lang = "en";
    filters.put(WCMComposer.FILTER_ORDER_BY, orderBy);
    filters.put(WCMComposer.FILTER_ORDER_TYPE, orderType);
    filters.put(WCMComposer.FILTER_LANGUAGE, lang);
    filters.put(WCMComposer.FILTER_RECURSIVE, recursive);

    String path=null;
    if (folderPath!=null) {
    	path = folderPath;
    }
    
    
    List<Node> nodes = wcmComposer.getContents(repositoryName, workspaceName, path, filters, WCMCoreUtils.getUserSessionProvider());
    
    String feedXML = generateRSS(nodes, contextRss);
    
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(feedXML.getBytes()));
    
    DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
    Response response = Response.ok(new DOMSource(document), MediaType.TEXT_XML).header(LAST_MODIFIED_PROPERTY, dateFormat.format(new Date())).build();
    return response;
  }
  
  /**
   * Generate rss.
   * 
   * @param context the context
   * 
   * @return the string
   */
  private String generateRSS(List<Node> nodes , Map<String, String> context) {  
    String rssVersion = (String) context.get(RSS_VERSION) ;
    String feedTitle = (String) context.get(FEED_TITLE) ;    
    String feedDescription = (String) context.get(DESCRIPTION) ;
    String feedLink = (String) context.get(LINK) ;
    String detailPage = (String) context.get(DETAIL_PAGE) ;
    String detailParam = (String) context.get(DETAIL_PARAM) ;
    String repository = (String) context.get(REPOSITORY) ;
    String workspace = (String) context.get(WORKSPACE) ;
    String contentUrl;
    if (!feedLink.endsWith("/") ) {
    	contentUrl= feedLink + "/" + detailPage + "?" + detailParam + "=/" + repository + "/" + workspace;
    }else {
    	contentUrl= feedLink + detailPage + "?" + detailParam + "=/" + repository + "/" + workspace;
    }

    if(feedTitle == null || feedTitle.length() == 0) feedTitle = "" ;
    try {
      
      SyndFeed feed = new SyndFeedImpl();      
      feed.setFeedType(rssVersion);      
      feed.setTitle(feedTitle.replaceAll("&nbsp;", " "));
      feed.setLink(feedLink);
      feed.setDescription(feedDescription.replaceAll("&nbsp;", " "));
      feed.setEncoding("UTF-8");
      
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      Iterator<Node> iter = nodes.iterator();
      while (iter.hasNext()) {        
        Node node = iter.next();
        String url = contentUrl + node.getPath() ;
        SyndEntry entry = new SyndEntryImpl();
        
        if (node.hasProperty(TITLE)) {
          String nTitle = node.getProperty(TITLE).getString();
          //encoding
          nTitle = new String(nTitle.getBytes("UTF-8"));
          entry.setTitle(Text.encodeIllegalXMLCharacters(nTitle));                
        }
        else entry.setTitle("") ;
        
        entry.setLink(url);        
        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        
        if (node.hasProperty(SUMMARY)) description.setValue(Text.encodeIllegalXMLCharacters(node.getProperty(SUMMARY).getString()).replaceAll("&nbsp;", " "));
        else description.setValue("") ;
        
        entry.setDescription(description);        
        entries.add(entry);
        entry.getEnclosures() ;
      }      
      feed.setEntries(entries);      
           
      SyndFeedOutput output = new SyndFeedOutput();      
      String feedXML = output.outputString(feed);      
      feedXML = StringUtils.replace(feedXML,"&amp;","&");
      return feedXML;
    } catch (Exception e) {
      log.error("Error when perform generateRSS: ", e);
    }
    return null;
  }

//  /**
//   * Gets the entry url.
//   * 
//   * @return the entry url
//   */
//  private String getEntryUrl(String link, String detailPage, String detailParam, String contentPath) {
//    return link + "/" + detailPage + "?" + detailParam + "=" +contentPath;
//  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.BaseConnector#getContentStorageType()
   */
  protected String getContentStorageType() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.BaseConnector#getRootContentStorage(javax.jcr.Node)
   */
  protected Node getRootContentStorage(Node node) throws Exception {
    return null;
  }
}
