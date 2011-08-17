﻿/**
 * Variable to check if some content has changed
 * - true if some content has changed
 **/
var b_changed = false;
var document_closing_confirm_msg = "";
var document_navigating_confirm_msg = "";

/**
 * Change the current state to inform some content has changed
 **/
function changed() {
  b_changed = true;
}

/**
 * UPDATE AJAX GET METHOD
 * - manage changes popup
 **/

function ajaxGet(url, callback) {
  var bypassActionbar= -1; //url.indexOf("uicomponent=UIActionBar_");
  if (b_changed && bypassActionbar<=0) {
    var answer = confirm(document_navigating_confirm_msg);
    if (answer) {
      b_changed = false;
    } else {
      return;
    }
    
  }

  if (!callback) callback = null ;
  doRequest("Get", url, null, callback) ;
};

/**
 * UPDATE FORM SUBMIT METHOD
 * - manage changes popup
 * - manage CKeditor update in textareas
 **/
function setClosingConfirmationMsg(msg) {
  document_closing_confirm_msg = msg;
}

function setNavigatingConfirmationMsg(msg) {
  document_navigating_confirm_msg = msg;
}
UIForm.prototype.submitForm = function(formId, action, useAjax, callback) {
 if(action.toLowerCase() == "close") {
   if (b_changed) {      
			var answer = confirm(document_closing_confirm_msg);
    	if (answer) {
      	b_changed = false;
    	} 
			else {
      	return;
    	}
   }
 }
 
 if (!callback) callback = null;
 var form = this.getFormElemt(formId) ;
 //TODO need review try-cactch block for form doesn't use FCK
 try {
  if (FCKeditorAPI && typeof FCKeditorAPI == "object") {
    for ( var name in FCKeditorAPI.__Instances ) {
      var oEditor ;
      try {
        oEditor = FCKeditorAPI.__Instances[name] ;
        if (oEditor && oEditor.GetParentForm && oEditor.GetParentForm() == form ) {
          oEditor.UpdateLinkedField() ;
        }
      } catch(e) {
        continue ;
      }
    }
  }
 } catch(e) {}

 try {
  
  if (CKEDITOR && typeof CKEDITOR == "object") {
    for ( var name in CKEDITOR.instances ) {
      var oEditor ;
      try {
        oEditor = CKEDITOR.instances[name] ;
        if (oEditor && document.getElementById(name)) {
            var rendered = eXo.core.DOMUtil.findNextElementByTagName(document.getElementById(name), 'span').id.indexOf('cke');
            if (rendered == 0) document.getElementById(name).value = oEditor.getData();
        }
      } catch(e) {
        continue ;
      }
    }
  }
 } catch(e) {}

  form.elements['formOp'].value = action ;
  if(useAjax) {
    b_changed = false;
    ajaxPost(form, callback) ;
  } else {
    form.submit();
  }
} ;
eXo.webui.UIForm = new UIForm();
/**
 * END UPDATE FORM
 **/

/**
 * Submits a form by Ajax, with the given action and the given parameters
 * Calls ajaxPost of PortalHttpRequest
 * Note: ie bug  you cannot have more than one button tag
 */
UIForm.prototype.submitEvent = function(formId, action, params) {
  var form = this.getFormElemt(formId) ;
	 try {
	  if (FCKeditorAPI && typeof FCKeditorAPI == "object") {
	 	  for ( var name in FCKeditorAPI.__Instances ) {
	 	  	var oEditor = FCKeditorAPI.__Instances[name] ;
	 	  	if ( oEditor.GetParentForm && oEditor.GetParentForm() == form ) {
	 	  		oEditor.UpdateLinkedField() ;
	 	  	}
	  	}
	  }
	 } catch(e) {}
	 
	  try {

	  if (CKEDITOR && typeof CKEDITOR == "object") {
	    for ( var name in CKEDITOR.instances ) {
	      var oEditor ;
	      try {
	        oEditor = CKEDITOR.instances[name] ;
	        if (oEditor && document.getElementById(name)) {
	            var rendered = eXo.core.DOMUtil.findNextElementByTagName(document.getElementById(name), 'span').id.indexOf('cke');
	            if (rendered == 0) document.getElementById(name).value = oEditor.getData();
	        }
	      } catch(e) {
	        continue ;
	      }
	    }
	  }
	 } catch(e) {}

	 
  form.elements['formOp'].value = action ; 
  if(!form.originalAction) form.originalAction = form.action ; 
	form.action =  form.originalAction +  encodeURI(params) ;
  b_changed = false;
  ajaxPost(form) ;
} ;

/**
 * Before we change the url, we check if the content has changed
 * Inform the user with a popup
 **/

function closeIt(e) {
  if (b_changed) {
    var e = e || window.event;
    // For IE and Firefox
    if (e) {
      e.returnValue = 'The changes you made will be lost if you navigate away from this page.';
    }
  
    // For Safari
    return 'The changes you made will be lost if you navigate away from this page.';
  }
}

/**
 * Catch any url changes in the browser
 **/
window.onbeforeunload = closeIt;

/**
 * Catch when some content has changed in the form
 **/
document.getElementById("UIDocumentForm").onchange = changed;


/**
 * Update each textarea when you type inside CKEditor
 * Inform the page that some content has changed
 **/
 try {
  if (CKEDITOR && typeof CKEDITOR == "object") {
    for ( var name in CKEDITOR.instances ) {
      var oEditor ;
      try {
        oEditor = CKEDITOR.instances[name] ;
        /**
         * inform the content has changed
         * update the textarea with last modifiedcontent
         */
		oEditor.on( 'key', function() {
		  b_changed = true;
        });



      } catch(e) {
        continue ;
      }
    }
  }
 } catch(e) {}

