package com.endava.ai.core.reporting.adapters.extent.patch.navigation;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchOperation;
import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;

import java.util.List;

public final class ExtentNavigationPatchPlanner {
    private static final String VIEW_SELECTOR = ".view";
    private static final String NAV_ITEM_SELECTOR = ".side-nav-menu .nav-item,.side-nav .nav-item,.side-nav li";
    private static final String NAV_LINK_SELECTOR = "#"
            + ExtentDomIds.NAV_TEST + ",#" + ExtentDomIds.NAV_CATEGORY + ",#" + ExtentDomIds.NAV_DASHBOARD;

    private ExtentNavigationPatchPlanner() {
    }

    public static ExtentHtmlPatchPlan plan(ExtentHtmlDocument document) {
        if (document.contains(ExtentMarkers.NAVIGATION_GUARD)
                && document.contains("activate(visibleView());")) {
            return ExtentHtmlPatchPlan.of(List.of());
        }
        if (document.contains(ExtentMarkers.NAVIGATION_GUARD)) {
            return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                    "extent.navigation.guard.upgrade",
                    "",
                    ExtentNavigationPatchPlanner::replaceNavigationGuard
            )));
        }
        return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                "extent.navigation.guard",
                ExtentMarkers.NAVIGATION_GUARD,
                ExtentNavigationPatchPlanner::injectNavigationGuard
        )));
    }

    private static String injectNavigationGuard(String html) {
        return injectBeforeBodyEnd(html, navigationGuardScript());
    }

    private static String replaceNavigationGuard(String html) {
        int markerIndex = html.indexOf(ExtentMarkers.NAVIGATION_GUARD);
        if (markerIndex < 0) {
            return injectNavigationGuard(html);
        }
        int scriptStart = html.lastIndexOf("<script", markerIndex);
        int scriptEnd = html.indexOf("</script>", markerIndex);
        if (scriptStart < 0 || scriptEnd < markerIndex) {
            return injectNavigationGuard(html);
        }
        return html.substring(0, scriptStart)
                + navigationGuardScript()
                + html.substring(scriptEnd + "</script>".length());
    }

    private static String navigationGuardScript() {
        return "<script id=\"" + ExtentMarkers.NAVIGATION_GUARD + "\">(function(){"
                + String.join("", List.of(
                "var views=['" + ExtentDomIds.TEST_VIEW + "','" + ExtentDomIds.CATEGORY_VIEW + "','"
                        + ExtentDomIds.DASHBOARD_VIEW + "'];",
                "function activate(view){",
                "document.querySelectorAll('" + NAV_ITEM_SELECTOR + "').forEach(function(node){node.classList.remove('active');});",
                "var active=document.querySelector('#nav-' + view.replace('-view',''))?.closest('.nav-item,li');",
                "if(active){active.classList.add('active');}",
                "}",
                "function show(view){",
                "document.querySelectorAll('" + VIEW_SELECTOR + "').forEach(function(node){node.classList.add('hide');node.classList.add('d-none');node.style.display='none';});",
                "document.querySelectorAll('.' + view).forEach(function(node){node.classList.remove('hide');node.classList.remove('d-none');node.style.display='';node.style.visibility='visible';});",
                "activate(view);",
                "}",
                "function visibleView(){",
                "for(var i=0;i<views.length;i++){",
                "var node=document.querySelector('.' + views[i]);",
                "if(node&&!node.classList.contains('hide')&&!node.classList.contains('d-none')&&node.style.display!=='none'){return views[i];}",
                "}",
                "return '" + ExtentDomIds.DASHBOARD_VIEW + "';",
                "}",
                "function navClick(event){",
                "var link=event.target&&event.target.closest&&event.target.closest('" + NAV_LINK_SELECTOR + "');",
                "if(!link){return;}",
                "event.preventDefault();",
                "event.stopPropagation();",
                "var view=link.id.replace('nav-','') + '-view';",
                "if(views.indexOf(view)>=0){show(view);}",
                "}",
                "function bindLinks(){",
                "views.forEach(function(view){",
                "var id='#nav-' + view.replace('-view','');",
                "var link=document.querySelector(id);",
                "if(link&&link.getAttribute('" + ExtentDataAttributes.NAV_BOUND + "')==='true'){return;}",
                "if(link){link.addEventListener('click',function(event){event.preventDefault();event.stopPropagation();show(view);});}",
                "if(link){link.setAttribute('" + ExtentDataAttributes.NAV_BOUND + "','true');}",
                "});",
                "}",
                "document.addEventListener('click',navClick,true);",
                "bindLinks();",
                "document.addEventListener('DOMContentLoaded',function(){",
                "bindLinks();",
                "activate(visibleView());",
                "});",
                "setTimeout(function(){activate(visibleView());},0);",
                "window.atfShowExtentView=show;"
        )) + "})();</script>";
    }

    static String injectBeforeBodyEnd(String html, String content) {
        int bodyEnd = html.lastIndexOf("</body>");
        if (bodyEnd < 0) {
            return html + content;
        }
        return html.substring(0, bodyEnd) + content + html.substring(bodyEnd);
    }
}
