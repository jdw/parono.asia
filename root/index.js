window.onload = function() {
	fetch(window.location.href + "rss.xml")
	  .then(response => response.text())
	  .then(str => new window.DOMParser().parseFromString(str, "text/xml"))
	  .then(data => {
		 console.log(data);
		 const items = data.querySelectorAll("item");
		 let markdown = "";

		 items.forEach(item => {
			markdown += "## " + item.querySelector("title").innerHTML + "\n";
			markdown += "    " + item.querySelector("pubDate").innerHTML + "\n\n";
			markdown += item.querySelector("description").innerHTML + "\n\n";
			markdown += "[Click]("+item.querySelector("link").innerHTML+") for more!\n"
		 });

		 render_markdown_from_string(markdown, "ebm");
	  });
}