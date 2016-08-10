<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="style.css">
<title>Search Tapesko</title>
</head>
<body>
    <a href="/"><img class="home" src="resources/tapeskoLogo.png"></a>
    <form action="/wikisearch" method="get">
	  <input class="home" type="text" name="term" placeholder="Search for something..."/>
	  <input class="home" type="submit" value="Search" ><br />
	</form>

<p>
	<% String results = (String) request.getAttribute("resultsId"); %>
	<%=results%>
</p>

</body>
</html>