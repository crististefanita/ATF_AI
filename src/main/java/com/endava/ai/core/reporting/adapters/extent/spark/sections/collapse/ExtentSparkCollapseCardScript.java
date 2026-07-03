package com.endava.ai.core.reporting.adapters.extent.spark.sections.collapse;

final class ExtentSparkCollapseCardScript {

    private ExtentSparkCollapseCardScript() {
    }

    static String javascript() {
        return "function setCardCollapsed(card,collapsed){"
                + "if(!hasDirectChildBodies(card)){return;}"
                + "card.classList.toggle(collapsedClass,collapsed);"
                + "directChildBodies(card).forEach(function(body){"
                + "if(body.classList.contains('collapse')){body.classList.toggle('show',!collapsed);}"
                + "});"
                + "card.querySelectorAll(':scope > .card-header [aria-expanded]').forEach(function(node){"
                + "node.setAttribute('aria-expanded',collapsed?'false':'true');"
                + "});"
                + "}"
                + "function collapseDescendantCards(card){"
                + "directChildBodies(card).forEach(function(body){"
                + "body.querySelectorAll('.card.'+initializedClass).forEach(function(child){"
                + "child.classList.remove(userExpandedClass);"
                + "setCardCollapsed(child,true);"
                + "});"
                + "});"
                + "}"
                + "function togglePreparedCard(card,event){"
                + "event.preventDefault();"
                + "event.stopPropagation();"
                + "var expanding=card.classList.contains(collapsedClass);"
                + "card.classList.toggle(userExpandedClass,expanding);"
                + "setCardCollapsed(card,!expanding);"
                + "if(expanding){collapseDescendantCards(card);}"
                + "}"
                + "function prepareCard(card){"
                + "markNodeScope(card);"
                + "ensureArrow(card);"
                + "if(!hasDirectChildBodies(card)){card.classList.add(leafClass);return;}"
                + "card.classList.remove(leafClass);"
                + "if(!card.classList.contains(initializedClass)){"
                + "card.classList.add(initializedClass);"
                + "setCardCollapsed(card,true);"
                + "}"
                + "if(!card.classList.contains(userExpandedClass)){setCardCollapsed(card,true);}"
                + "}"
                + "window.collapseExtentTreeByDefault=function(){"
                + "injectArrowOverrideStyle();"
                + "normalizeStatusMenu();"
                + "document.querySelectorAll('.test-contents .card').forEach(prepareCard);"
                + "document.querySelectorAll('.test-contents [aria-expanded=\"true\"]').forEach(function(node){node.setAttribute('aria-expanded','false');});"
                + "};";
    }
}
