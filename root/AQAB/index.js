window.onload = function() {
	render_markdown(window.location.pathname + "/index.md", "ebm")
	render_markdown(window.location.pathname + "/instructions.md", "instructions")
}