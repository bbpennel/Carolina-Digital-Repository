define('ResultObjectActionMenu', [ 'jquery', 'jquery-ui', 'DeleteObjectButton', 'PublishObjectButton', 'ReindexObjectButton', 'contextMenu'],
		function($, ui, DeleteObjectButton, PublishObjectButton, ReindexObjectButton) {
	
	var defaultOptions = {
		selector : undefined,
		containerSelector : undefined,
		trigger : 'left',
		positionAtTrigger : true
	};
	
	function ResultObjectActionMenu(options) {
		this.options = $.extend({}, defaultOptions, options);
		this.create();
	};
	
	ResultObjectActionMenu.prototype.create = function() {
		var self = this;
		var menuOptions = {
			selector: this.options.selector,
			trigger: this.options.trigger,
			events : {
				show: function() {
					this.parents(self.options.containerSelector).find(".action_gear").attr("src", "/static/images/admin/gear_dark.png");
				},
				hide: function() {
					this.parents(self.options.containerSelector).find(".action_gear").attr("src", "/static/images/admin/gear.png");
				}
			},
			build: function($trigger, e) {
				var resultObject = $trigger.parents(self.options.containerSelector).data('resultObject');
				var metadata = resultObject.metadata;
				var items = {};
				if ($.inArray('publish', metadata.permissions) != -1)
					items["publish"] = {name : $.inArray('Unpublished', metadata.status) == -1 ? 'Unpublish' : 'Publish'};
				if ($.inArray('editAccessControl', metadata.permissions) != -1) 
					items["editAccess"] = {name : 'Edit Access'};
				if ($.inArray('editDescription', metadata.permissions) != -1)
					items["editDescription"] = {name : 'Edit Description'};
				if ($.inArray('purgeForever', metadata.permissions) != -1) {
					items["purgeForever"] = {name : 'Delete'};
					items["reindex"] = {name : 'Reindex'};
				}
				
				return {
					callback: function(key, options) {
						switch (key) {
							case "publish" :
								var publishButton = new PublishObjectButton({
									pid : resultObject.pid,
									parentObject : resultObject,
									defaultPublish : $.inArray("Unpublished", resultObject.metadata.status) == -1,
									metadata : metadata
								});
								publishButton.activate();
								break;
							case "editAccess" :
								self.editAccess(resultObject);
								break;
							case "editDescription" :
								// Resolve url to be absolute for IE, which doesn't listen to base tags when dealing with javascript
								var url = document.location.href;
								url = url.substring(0, url.indexOf("/admin/") + 7);
								document.location.href = url + "describe/" + metadata.id;
								break;
							case "purgeForever" :
								var deleteButton = new DeleteObjectButton({
									pid : resultObject.pid,
									parentObject : resultObject,
									metadata : metadata,
									confirmAnchor : options.$trigger
								});
								deleteButton.activate();
								break;
							case "reindex" :
								var reindexButton = new ReindexObjectButton({
									pid : resultObject.pid,
									parentObject : resultObject,
									metadata : metadata,
									confirmAnchor : options.$trigger
								});
								reindexButton.activate();
								break;
						}
					},
					items: items
				};
			}
		};
		if (self.options.positionAtTrigger)
			menuOptions.position = function(options, x, y) {
				options.$menu.position({
					my : "right top",
					at : "right bottom",
					of : options.$trigger
				});
			};
		$.contextMenu(menuOptions);
	};
	
	ResultObjectActionMenu.prototype.editAccess = function(resultObject) {
		var dialog = $("<div class='containingDialog'><img src='/static/images/admin/loading_large.gif'/></div>");
		dialog.dialog({
			autoOpen: true,
			width: 500,
			height: 'auto',
			maxHeight: 800,
			minWidth: 500,
			modal: true,
			title: 'Access Control Settings',
			close: function() {
				dialog.remove();
				resultObject.unhighlight();
			}
		});
		dialog.load("acl/" + resultObject.metadata.id, function(responseText, textStatus, xmlHttpRequest){
			dialog.dialog('option', 'position', 'center');
		});
	};
	
	return ResultObjectActionMenu;
});