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
package org.exoplatform.services.wcm.search.connector;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.ResultNode;
import org.exoplatform.services.wcm.search.base.AbstractPageList;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 5, 2013  
 */
public abstract class BaseContentSearchServiceConnector extends BaseSearchServiceConnector {

  private static final Log LOG = ExoLogger.getLogger(BaseContentSearchServiceConnector.class.getName());
  
  public BaseContentSearchServiceConnector(InitParams initParams) throws Exception {
    super(initParams);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected QueryCriteria createQueryCriteria(String query, long offset, long limit, String sort, String order) {
    QueryCriteria criteria = new QueryCriteria();
    //set content types
    criteria.setContentTypes(getSearchedDocTypes());
    criteria.setKeyword(query.toLowerCase());
    criteria.setSearchWebpage(false);
    criteria.setSearchDocument(true);
    criteria.setSearchWebContent(true);
    criteria.setMultiplePhaseSearch(true);
    criteria.setLiveMode(true);
    criteria.setOffset(offset);
    criteria.setLimit(limit);
    criteria.setSortBy(sort);
    criteria.setOrderBy(order);
    return criteria;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractPageList<ResultNode> searchNodes(QueryCriteria criteria) throws Exception {
    return siteSearch_.searchSiteContents(WCMCoreUtils.getUserSessionProvider(),
                                           criteria, (int)criteria.getLimit(), true);
  }

  @Override
  protected String getPath(DriveData driveData, ResultNode node) throws Exception {
    String subPath = node.getPath().substring(driveData.getHomePath().length());
    if (!subPath.startsWith("/")) {
      subPath = '/' + subPath;
    }
    return '/' + driveData.getName() + subPath;
  }
  
  @Override
  protected String getFileType(ResultNode node) throws Exception {
    return org.exoplatform.services.cms.impl.Utils.getNodeTypeIcon(node, "").
            replace(".", "").replace("/", "").replace("\\","").replace(".", "");
  }

  /**
   * returns the primary types of the specific search service:
   * nt:file for file search, all other document types for document search
   * @return searched doc types
   */
  protected abstract String[] getSearchedDocTypes();

}

