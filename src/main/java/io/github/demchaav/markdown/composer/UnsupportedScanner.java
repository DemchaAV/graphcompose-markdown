package io.github.demchaav.markdown.composer;

import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.FootnoteDefinitionNode;
import io.github.demchaav.markdown.model.FootnotesNode;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.ListItemNode;
import io.github.demchaav.markdown.model.ListNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.QuoteNode;
import io.github.demchaav.markdown.model.TableCellNode;
import io.github.demchaav.markdown.model.TableNode;
import io.github.demchaav.markdown.model.UnsupportedBlockNode;
import io.github.demchaav.markdown.model.inline.EmphasisRun;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.model.inline.StrikethroughRun;
import io.github.demchaav.markdown.model.inline.StrongRun;
import io.github.demchaav.markdown.model.inline.UnsupportedInlineRun;

import java.util.List;

/**
 * Walks a {@link MarkdownDocument} for unsupported content, so the composer can
 * enforce strict mode. Returns the first offender (depth-first), or {@code null} when
 * every node can be rendered faithfully.
 */
final class UnsupportedScanner {

    private UnsupportedScanner() {
    }

    static String firstUnsupported(MarkdownDocument document) {
        return scanBlocks(document.blocks());
    }

    private static String scanBlocks(List<MarkdownNode> blocks) {
        for (MarkdownNode block : blocks) {
            String found = scanBlock(block);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static String scanBlock(MarkdownNode node) {
        if (node instanceof UnsupportedBlockNode unsupported) {
            return "unsupported block <" + unsupported.kind() + ">: " + preview(unsupported.raw());
        } else if (node instanceof HeadingNode heading) {
            return scanInlines(heading.content());
        } else if (node instanceof ParagraphNode paragraph) {
            return scanInlines(paragraph.content());
        } else if (node instanceof QuoteNode quote) {
            return scanBlocks(quote.content());
        } else if (node instanceof CustomBlockNode custom) {
            return scanBlocks(custom.content());
        } else if (node instanceof ListNode list) {
            for (ListItemNode item : list.items()) {
                String found = scanBlocks(item.content());
                if (found != null) {
                    return found;
                }
            }
        } else if (node instanceof TableNode table) {
            String found = scanCells(table.header());
            if (found != null) {
                return found;
            }
            for (List<TableCellNode> row : table.bodyRows()) {
                found = scanCells(row);
                if (found != null) {
                    return found;
                }
            }
        } else if (node instanceof FootnotesNode footnotes) {
            for (FootnoteDefinitionNode definition : footnotes.definitions()) {
                String found = scanBlocks(definition.content());
                if (found != null) {
                    return found;
                }
            }
        }
        // CodeBlockNode, ThematicBreakNode, ImageNode carry no unsupported children.
        return null;
    }

    private static String scanCells(List<TableCellNode> cells) {
        for (TableCellNode cell : cells) {
            String found = scanInlines(cell.content());
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static String scanInlines(List<InlineNode> nodes) {
        for (InlineNode node : nodes) {
            String found = scanInline(node);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static String scanInline(InlineNode node) {
        if (node instanceof UnsupportedInlineRun unsupported) {
            return "unsupported inline: " + preview(unsupported.raw());
        } else if (node instanceof StrongRun strong) {
            return scanInlines(strong.children());
        } else if (node instanceof EmphasisRun emphasis) {
            return scanInlines(emphasis.children());
        } else if (node instanceof StrikethroughRun strikethrough) {
            return scanInlines(strikethrough.children());
        } else if (node instanceof LinkRun link) {
            return scanInlines(link.children());
        }
        return null;
    }

    private static String preview(String raw) {
        String oneLine = raw.strip().replace('\n', ' ');
        return oneLine.length() > 60 ? oneLine.substring(0, 57) + "..." : oneLine;
    }
}
