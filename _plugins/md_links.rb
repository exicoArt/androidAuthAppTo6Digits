# Rewrites relative links to Markdown files (for example "threat-model.md") into links to the
# built pages ("threat-model.html"), so the same Markdown works both when browsing the repository
# on GitHub and on the published site. Absolute links and in-page anchors are left alone.
Jekyll::Hooks.register :pages, :post_convert do |page|
  page.content = page.content.gsub(/href="((?![a-zA-Z][a-zA-Z0-9+.-]*:|#|\/)[^"#?]*?)\.md((?:#[^"]*)?)"/) do
    "href=\"#{Regexp.last_match(1)}.html#{Regexp.last_match(2)}\""
  end
end
