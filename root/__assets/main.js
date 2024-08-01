function render_markdown(file, id) {
	var req = new XMLHttpRequest();
	req.onload = function(){
		var text = this.responseText;
		var converter = new showdown.Converter({tables: true});
		var html = converter.makeHtml(text);
		document.getElementById(id).innerHTML = html;
	};
	req.open('GET', file);
	req.send();
}

function render_markdown_from_string(text, id) {
	var converter = new showdown.Converter({tables: true});
	var html = converter.makeHtml(text);
	document.getElementById(id).innerHTML = html;
}