package com.ai.atf.core.reporting.adapters.dom;

import com.microsoft.playwright.Page;

import java.util.Locale;
import java.util.Map;

public final class ExtentSparkDomState {

    private ExtentSparkDomState() {
    }

    public static String tagFilterState(Page page) {
        return String.valueOf(page.evaluate("() => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const side = Array.from(document.querySelectorAll('.test-list .test-list-item, .test-list li, .test-list .test-item'))"
                + ".filter(visible).map(el => (el.innerText || el.textContent || '').trim().replace(/\\s+/g, ' '));"
                + "const main = (document.querySelector('.test-content')?.innerText || "
                + "document.querySelector('.main-content')?.innerText || '').replace(/\\s+/g, ' ').slice(0, 1200);"
                + "return JSON.stringify({side, main});"
                + "}"));
    }

    public static String mainFilterState(Page page) {
        return String.valueOf(page.evaluate("() => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "return Array.from(document.querySelectorAll('.test-contents > .card, .test-content, .main-content'))"
                + ".filter(visible).map(el => (el.innerText || el.textContent || '').trim().replace(/\\s+/g, ' '))"
                + ".join(' | ');"
                + "}"));
    }

    public static String collectionShellState(Page page) {
        return String.valueOf(page.evaluate("() => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const shell = document.querySelector('.atf-runtime-tag-selection');"
                + "const heading = shell ? (shell.querySelector('h5')?.textContent || '').trim() : 'missing';"
                + "const badge = shell ? (shell.querySelector('.badge-default')?.textContent || '').trim() : 'missing';"
                + "const items = shell ? Array.from(shell.querySelectorAll('.atf-runtime-tag-selected-item'))"
                + ".filter(visible).map(item => (item.textContent || '').replace(/\\s+/g, ' ').trim()).join('|') : '';"
                + "const nativeClassHeader = !shell && !!document.querySelector('.test-content > .test-contents');"
                + "return ['shell=' + !!shell, 'heading=' + heading, 'badge=' + badge,"
                + "'items=' + items, 'nativeClassHeader=' + nativeClassHeader].join(';');"
                + "}"));
    }

    public static boolean isVisibleText(Page page, String text) {
        String script = "needle => {"
                + "const visible = el => {"
                + "if (!el || !el.getClientRects().length) { return false; }"
                + "for (let node = el; node; node = node.parentElement) {"
                + "const style = getComputedStyle(node);"
                + "if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') { return false; }"
                + "}"
                + "return true;"
                + "};"
                + "return Array.from(document.querySelectorAll('td, textarea, span, a.node, pre, code')).some(el => "
                + "visible(el) && (el.textContent || '').toLowerCase().includes(needle));"
                + "}";
        return Boolean.TRUE.equals(page.evaluate(script, text.toLowerCase(Locale.ROOT)));
    }

    public static boolean isVisibleNode(Page page, String text) {
        return Boolean.TRUE.equals(page.evaluate("text => Array.from(document.querySelectorAll('a.node'))"
                + ".some(el => {"
                + "const card = el.closest('.card');"
                + "return (el.textContent || '').includes(text) && !!card "
                + "&& (card.offsetWidth || card.offsetHeight || card.getClientRects().length);"
                + "})", text));
    }

    public static int arrowCount(Page page, String text) {
        Object count = page.evaluate("text => {"
                + "const node = Array.from(document.querySelectorAll('a.node'))"
                + ".find(el => (el.textContent || '').includes(text));"
                + "return node ? node.querySelectorAll(':scope > .extent-atf-arrow').length : -1;"
                + "}", text);
        return ((Number) count).intValue();
    }

    public static String directNodeHeaderText(Page page, String text) {
        return String.valueOf(page.evaluate("text => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const card = Array.from(document.querySelectorAll('.test-contents .card'))"
                + ".find(card => visible(card) && (card.querySelector(':scope > .card-header a.node')?.textContent || '').includes(text));"
                + "return card ? (card.querySelector(':scope > .card-header')?.innerText || '').replace(/\\s+/g, ' ').trim() : '';"
                + "}", text));
    }

    public static String nativePseudoArrow(Page page, String text) {
        return String.valueOf(page.evaluate("text => {"
                + "const node = Array.from(document.querySelectorAll('a.node'))"
                + ".find(el => (el.textContent || '').includes(text));"
                + "if (!node) { return 'missing'; }"
                + "return getComputedStyle(node, '::before').content;"
                + "}", text));
    }

    public static String extentNodeDebug(Page page, String parentText, String childText) {
        return String.valueOf(page.evaluate("(args) => {"
                + "const norm = el => (el && el.textContent || '').trim().replace(/\\s+/g, ' ').slice(0, 80);"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const node = label => Array.from(document.querySelectorAll('a.node'))"
                + ".find(el => norm(el).includes(label));"
                + "const info = label => {"
                + "const n = node(label);"
                + "const c = n && n.closest('.card');"
                + "return {label, nodeFound: !!n, nodeVisible: visible(n), cardClass: c && c.className,"
                + "directAccordionDisplay: c && Array.from(c.children).filter(ch => ch.classList && ch.classList.contains('accordion')).map(ch => getComputedStyle(ch).display),"
                + "directCollapseDisplay: c && Array.from(c.children).filter(ch => ch.classList && ch.classList.contains('collapse')).map(ch => getComputedStyle(ch).display)};"
                + "};"
                + "return JSON.stringify({parent: info(args.parent), child: info(args.child)});"
                + "}", Map.of("parent", parentText, "child", childText)));
    }

    static String expandedNodeMetadata(Page page, String tag) {
        return String.valueOf(page.evaluate("tag => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const matchingItem = Array.from(document.querySelectorAll('.test-list li.test-item, .test-list .test-item'))"
                + ".find(node => visible(node) && (node.innerText || node.textContent || '').includes(tag));"
                + "if (matchingItem) { matchingItem.click(); }"
                + "if (window.collapseExtentTreeByDefault) { window.collapseExtentTreeByDefault(); }"
                + "for (let i = 0; i < 5; i++) {"
                + "Array.from(document.querySelectorAll('.test-contents .card.extent-atf-collapsed > .card-header .extent-atf-arrow'))"
                + ".filter(visible).forEach(node => node.click());"
                + "}"
                + "const rootAttrs = Array.from(document.querySelectorAll("
                + "'.test-contents .card.extent-atf-root-node > .card-header .node-attr, "
                + ".test-contents > .mt-4 > .accordion > .card > .card-header .node-attr, "
                + ".test-contents > .accordion > .card > .card-header .node-attr'))"
                + ".filter(visible);"
                + "const nestedAttrs = Array.from(document.querySelectorAll("
                + "'.test-contents .card .card > .card-header .node-attr, "
                + ".test-content .detail-body .card .card > .card-header .node-attr, "
                + ".test-content .detail-body .accordion .accordion .card > .card-header .node-attr'))"
                + ".filter(visible);"
                + "const nestedText = nestedAttrs.map(node => (node.innerText || node.textContent || '')"
                + ".replace(/\\s+/g, ' ').trim()).join('|');"
                + "return ["
                + "'rootVisibleNodeAttrs=' + rootAttrs.length,"
                + "'nestedVisibleNodeAttrs=' + nestedAttrs.length,"
                + "'nestedText=' + nestedText"
                + "].join(';');"
                + "}", tag));
    }
}
