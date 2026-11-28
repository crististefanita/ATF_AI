package com.ai.atf.core.reporting.adapters.dom;

import com.microsoft.playwright.Page;

public final class ExtentSparkDomActions {

    private ExtentSparkDomActions() {
    }

    public static boolean clickNode(Page page, String text) {
        return Boolean.TRUE.equals(page.evaluate("text => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const cards = Array.from(document.querySelectorAll('.test-contents .card'))"
                + ".filter(card => visible(card) && (card.querySelector(':scope > .card-header a.node')?.textContent || '').includes(text));"
                + "const header = cards[0]?.querySelector(':scope > .card-header');"
                + "if (!header) { return false; }"
                + "header.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));"
                + "return true;"
                + "}", text));
    }

    public static void waitForPreparedExtentTree(Page page) {
        page.waitForFunction("() => {"
                + "const hasAtfCollapse = typeof window.collapseExtentTreeByDefault === 'function';"
                + "const cards = document.querySelectorAll('.test-contents .card');"
                + "const prepared = document.querySelectorAll('.test-contents .card.extent-atf-collapsed, "
                + ".test-contents .card.extent-atf-leaf');"
                + "return hasAtfCollapse && (cards.length === 0 || prepared.length > 0);"
                + "}");
    }

    public static boolean selectExtentTag(Page page, String tag) {
        return Boolean.TRUE.equals(page.evaluate("tag => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const tagButton = document.querySelector('.test-list-tools .fa.fa-bug, .test-list-tools .fa.fa-tag')?.closest('a');"
                + "let selected = false;"
                + "if (tagButton) {"
                + "tagButton.click();"
                + "const menu = document.querySelector('#tag-toggle');"
                + "const item = menu && visible(menu) ? Array.from(menu.querySelectorAll('a, button, li')).find(el => "
                + "(el.textContent || '').trim().replace(/\\s+/g, ' ').replace(/^\\d+\\s*/, '') === tag) : null;"
                + "if (item) { item.click(); selected = true; }"
                + "}"
                + "if (!selected && typeof window.atfSelectExtentTag === 'function') {"
                + "window.atfSelectExtentTag(tag);"
                + "selected = true;"
                + "}"
                + "return selected;"
                + "}", tag));
    }

    public static String selectTagExpandAndReadNodeMetadata(Page page, String tag) {
        String selectionState = String.valueOf(page.evaluate("tag => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const trigger = document.querySelector('.test-list-tools .fa-bug')?.closest('a')"
                + " || document.querySelector('.test-list-tools .fa-tag')?.closest('a');"
                + "if (!trigger) { return 'missing-tag-trigger'; }"
                + "trigger.click();"
                + "const menu = document.querySelector('#tag-toggle');"
                + "if (!menu) { return 'missing-tag-menu'; }"
                + "menu.classList.add('show');"
                + "menu.style.display = 'block';"
                + "const item = Array.from(menu.querySelectorAll('.dropdown-item,[tag]'))"
                + ".find(node => visible(node) && (node.textContent || '').includes(tag));"
                + "if (!item) { return 'missing-tag-item:' + tag + ':menu='"
                + " + (menu.innerText || menu.textContent || '').replace(/\\s+/g, ' '); }"
                + "item.click();"
                + "const selected = Array.from(document.querySelectorAll('.test-list li.test-item, .test-list .test-item'))"
                + ".filter(visible).map(node => (node.innerText || node.textContent || '').replace(/\\s+/g, ' ').trim())"
                + ".join('|');"
                + "return 'selected=' + selected;"
                + "}", tag));
        page.waitForTimeout(500);
        waitForPreparedExtentTree(page);
        return selectionState + ";" + ExtentSparkDomState.expandedNodeMetadata(page, tag);
    }
}
