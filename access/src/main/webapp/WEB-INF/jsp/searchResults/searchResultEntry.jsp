<%--

    Copyright 2008 The University of North Carolina at Chapel Hill

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="cdr" uri="http://cdr.lib.unc.edu/cdrUI" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="accessGroupConstants" class="edu.unc.lib.dl.acl.util.AccessGroupConstants" scope="request"/>

<c:choose>
	<c:when test="${param.resultNumber % 2 == 0 }">
		<c:set var="resultEntryClass" value="even" scope="page"/>
	</c:when>
	<c:otherwise>
		<c:set var="resultEntryClass" value="" scope="page"/>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${not empty metadata.countMap}">
		<c:set var="childCount" value="${metadata.countMap.child}"/>
	</c:when>
	<c:otherwise>
		<c:set var="childCount" value="0"/>
	</c:otherwise>
</c:choose>
<div id="entry${metadata.id}" class="searchitem ${resultEntryClass}">
	<div class="contentarea">
		<%-- Link to full record of the current item --%>
		<c:url var="fullRecordUrl" scope="page" value="record">
			<c:param name="${searchSettings.searchStateParams['ID']}" value="${metadata.id}"/>
		</c:url>
		<c:url var="containerResultsUrl" scope="page" value='search'>
			<c:param name="${searchSettings.searchStateParams['FACET_FIELDS']}" value="${searchSettings.searchFieldParams['ANCESTOR_PATH']}:${metadata.path.limitToValue}"/>
		</c:url>
		<%-- Set primary action URL based on content model and container results URL as appropriate --%>
		<c:choose>
			<c:when test="${metadata.resourceType == searchSettings.resourceTypeFolder}">
				<c:set var="primaryActionUrl" scope="page" value="${containerResultsUrl}"/>
				<c:set var="primaryActionTooltip" scope="page" value="View the contents of this folder."/>
			</c:when>
			<c:otherwise>
				<c:set var="primaryActionUrl" scope="page" value="${fullRecordUrl}"/>
				<c:set var="primaryActionTooltip" scope="page" value="View details for ${metadata.title}."/>
			</c:otherwise>
		</c:choose>
		<%-- Display thumbnail or placeholder graphic --%>
		<a href="<c:out value='${primaryActionUrl}' />" title="${primaryActionTooltip}" class="has_tooltip">
			<c:choose>
				<c:when test="${cdr:permitDatastreamAccess(requestScope.accessGroupSet, 'THUMB_SMALL', metadata)}">
					<div class="smallthumb_container">
						<img id="thumb_${param.resultNumber}" class="smallthumb ph_small_${metadata.contentTypeFacet[0].searchKey}" 
								src="${cdr:getDatastreamUrl(metadata, 'THUMB_SMALL', fedoraUtil)}"/>
					</div>
				</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${metadata.resourceType == searchSettings.resourceTypeFolder}">
							<div class="smallthumb_container">
								<img class="smallthumb" src="/static/images/placeholder/small/folder.png"/>
							</div>
						</c:when>
						<c:when test="${metadata.resourceType == searchSettings.resourceTypeCollection}">
							<div class="smallthumb_container">
								<img id="thumb_${param.resultNumber}" class="smallthumb ph_small_clear" 
										src="/static/images/collections/${metadata.idWithoutPrefix}.jpg" style="height: 64px; width: 64px;"/>
							</div>
						</c:when>
						<c:when test="${metadata.resourceType == searchSettings.resourceTypeAggregate && empty metadata.contentTypeFacet[0].searchKey}">
							<div class="smallthumb_container">
								<img class="smallthumb" src="/static/images/placeholder/small/default.png"/>
							</div>
						</c:when>
						<c:otherwise>
							<div class="smallthumb_container">
								<img id="thumb_${param.resultNumber}" class="smallthumb ph_small_default" 
										src="/static/images/placeholder/small/${metadata.contentTypeFacet[0].searchKey}.png"/>
							</div>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</a>
		<%-- Main result entry metadata body --%>
		<div class="iteminfo">
			<c:choose>
				<%-- Metadata body for containers --%>
				<c:when test="${metadata.resourceType == searchSettings.resourceTypeCollection || metadata.resourceType == searchSettings.resourceTypeFolder}">
					<h2><a href="<c:out value='${primaryActionUrl}' />" title="${primaryActionTooltip}" class="has_tooltip"><c:out value="${metadata.title}"/></a>
						<c:if test="${metadata.resourceType == searchSettings.resourceTypeFolder}">
							<span class="searchitem_container_count">(${childCount} item<c:if test="${childCount != 1}">s</c:if>)</span>
						</c:if>
					</h2>
					<div class="halfwidth">
						<c:choose>
							<c:when test="${not empty metadata.creator}">
								<p>${searchSettings.searchFieldLabels['CREATOR']}:
									<c:forEach var="creatorObject" items="${metadata.creator}" varStatus="creatorStatus">
										<c:out value="${creatorObject}"/><c:if test="${!creatorStatus.last}">; </c:if>
									</c:forEach>
								</p>		
							</c:when>
							<c:otherwise>
								<p>${searchSettings.searchFieldLabels['DATE_ADDED']}: <fmt:formatDate pattern="yyyy-MM-dd" value="${metadata.dateAdded}" /></p>
							</c:otherwise>
						</c:choose>
					</div>
					<div class="halfwidth">
						<p>${searchSettings.searchFieldLabels['DATE_UPDATED']}: <fmt:formatDate pattern="yyyy-MM-dd" value="${metadata.dateUpdated}" /></p> 
					</div>
					<c:if test="${not empty metadata.abstractText}">
						<div class="clear"></div>
						<p>${searchSettings.searchFieldLabels['ABSTRACT']}: 
							<c:out value="${cdr:truncateText(metadata.abstractText, 250)}"/>
							<c:if test="${fn:length(metadata.abstractText) > 250}">...</c:if>
							</p>
					</c:if>
				</c:when>
				<%-- Metadata body for items --%>
				<c:when test="${metadata.resourceType == searchSettings.resourceTypeFile || metadata.resourceType == searchSettings.resourceTypeAggregate}">
					<h2><a href="<c:out value='${primaryActionUrl}' />"><c:out value="${metadata.title}"/></a>
						<c:if test="${metadata.resourceType == searchSettings.resourceTypeAggregate && childCount > 1}">
							<span class="searchitem_container_count">(${childCount} item<c:if test="${childCount != 1}">s</c:if>)</span>
						</c:if>
					</h2>
					<div class="halfwidth">
						<c:if test="${not empty metadata.creator}">
							<p>${searchSettings.searchFieldLabels['CREATOR']}:
								<c:choose>
									<c:when test="${fn:length(metadata.creator) > 5}"><c:set var="creatorList" value="${cdr:subList(metadata.creator, 0, 5)}"/></c:when>
									<c:otherwise><c:set var="creatorList" value="${metadata.creator}"/></c:otherwise>
								</c:choose>
								
								<c:forEach var="creatorObject" items="${creatorList}" varStatus="creatorStatus">
									<c:out value="${creatorObject}"/><c:if test="${!creatorStatus.last}">; </c:if>
								</c:forEach>
								
								<c:if test="${fn:length(metadata.creator) > 5}">; et al.</c:if>
							</p>
						</c:if>
						<p>
							<c:url var="parentUrl" scope="page" value="record">
								<c:param name="${searchSettings.searchStateParams['ID']}" value="${metadata.parentCollection}"/>
							</c:url>
							${searchSettings.searchFieldLabels['PARENT_COLLECTION']}: <a href="<c:out value='${parentUrl}' />"><c:out value="${metadata.parentCollectionObject.displayValue}"/></a>
						</p>
					</div>
					<div class="halfwidth">
						<p>${searchSettings.searchFieldLabels['DATE_ADDED']}: <fmt:formatDate pattern="yyyy-MM-dd" value="${metadata.dateAdded}" /></p>
						<c:if test="${not empty metadata.dateCreated}">
							<c:set var="dateCreatedMonthDay" scope="page"><fmt:formatDate pattern="MM-dd" timeZone="GMT" value="${metadata.dateCreated}" /></c:set>
							<p>${searchSettings.searchFieldLabels['DATE_CREATED']}: 
								<c:choose>
									<c:when test="${dateCreatedMonthDay == '01-01'}">
										<fmt:formatDate pattern="yyyy" timeZone="GMT" value="${metadata.dateCreated}" />
									</c:when>
									<c:otherwise>
										<fmt:formatDate pattern="yyyy-MM-dd" timeZone="GMT" value="${metadata.dateCreated}" />
									</c:otherwise>
								</c:choose>
							</p>
						</c:if>
					</div>
				</c:when>
			</c:choose>
		</div>
		<%-- Action buttons --%>
		<c:choose>
			<c:when test="${metadata.resourceType == searchSettings.resourceTypeFolder}">
				<div class="containerinfo">
					<c:url var="browseUrl" scope="page" value='browse?${searchStateUrl}'>
						<c:param name="${searchSettings.searchStateParams['FACET_FIELDS']}" value="${searchSettings.searchFieldParams['ANCESTOR_PATH']}:${metadata.path.searchValue}"/>
						<c:param name="${searchSettings.searchStateParams['ACTIONS']}" value="${searchSettings.actions['RESET_NAVIGATION']}:structure"/>
					</c:url>
					<ul>
						<li><a href="<c:out value='${fullRecordUrl}'/>" title="View folder information for ${metadata.title}" class="has_tooltip">View ${fn:toLowerCase(metadata.resourceType)} details</a></li>
						<li><a href="<c:out value='${browseUrl}'/>" title="View the structure of this folder in a file browser view." class="has_tooltip">Browse structure</a></li>
					</ul>
				</div>
			</c:when>
			<c:when test="${metadata.resourceType == searchSettings.resourceTypeCollection}">
				<div class="containerinfo">
					<c:url var="browseUrl" scope="page" value='browse?${searchStateUrl}'>
						<c:param name="${searchSettings.searchStateParams['FACET_FIELDS']}" value="${searchSettings.searchFieldParams['ANCESTOR_PATH']}:${metadata.path.searchValue}"/>
						<c:param name="${searchSettings.searchStateParams['ACTIONS']}" value="${searchSettings.actions['RESET_NAVIGATION']}:structure"/>
					</c:url>
					<ul>
						<li><a href="<c:out value='${containerResultsUrl}'/>" title="View the contents of this collection" class="has_tooltip">View ${childCount} items</a></li>
						<li><a href="<c:out value='${browseUrl}'/>" title="View the structure of this collection in a file browser view." class="has_tooltip">Browse structure</a></li>
						<li>${metadata.resourceType}</li>
					</ul>
				</div>
			</c:when>
			<c:when test="${metadata.resourceType == searchSettings.resourceTypeFile || metadata.resourceType == searchSettings.resourceTypeAggregate}">
				<div class="fileinfo">
					<c:choose>
						<c:when test="${cdr:permitDatastreamAccess(requestScope.accessGroupSet, 'DATA_FILE', metadata)}">
							<div class="actionlink right download">
								<a href="${cdr:getDatastreamUrl(metadata, 'DATA_FILE', fedoraUtil)}&dl=true">Download</a>
							</div>
						</c:when>
						<c:when test="${cdr:permitDatastreamAccess(requestScope.accessGroupSet, 'SURROGATE', metadata)}">
							<div class="actionlink right download">
								<a href="${cdr:getDatastreamUrl(metadata, 'SURROGATE', fedoraUtil)}">Preview</a>
							</div>
						</c:when>
						<c:when test="${metadata.resourceType == searchSettings.resourceTypeFile}">
							<div class="actionlink right login">
								<a href="${loginUrl}">Login</a>
							</div>
						</c:when>
					</c:choose>
					
					<c:if test="${metadata.resourceType == searchSettings.resourceTypeFile || (metadata.resourceType == searchSettings.resourceTypeAggregate && not empty metadata.contentTypeFacet)}">
						<p class="right">
							<c:out value="${metadata.contentTypeFacet[0].displayValue}"/>
							<c:if test="${not empty metadata.filesizeSort}">
								&nbsp;(<c:out value="${cdr:formatFilesize(metadata.filesizeSort, 1)}"/>)
							</c:if>
						</p>
					</c:if>
					
					<c:choose>
						<c:when test="${cdr:contains(metadata.readGroup, accessGroupConstants.PUBLIC_GROUP)}">
							
						</c:when>
						<c:otherwise>
							<p class="right">
								Restricted Access
							</p>
						</c:otherwise>
					</c:choose>
					
					<c:if test="${childCount > 1}">
						<p class="right">
							<a href="<c:out value='${containerResultsUrl}'/>" title="View all files contained in this item" class="has_tooltip">View ${childCount} items</a>
						</p>
					</c:if>
					
				</div>
			</c:when>
		</c:choose>
	</div>
</div>