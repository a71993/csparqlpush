<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html>
	<head>
		<script src="http://code.jquery.com/jquery-1.11.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/refresh.js"></script>
	</head>

	<body>
	
		<p><a href="../index.html">home</a></p>
		<table id="results">
		<thead>
			<tr>
				<th>subject</th>
				<th>predicate</th>
				<th>object</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="triples" items="${triples}">
			<tr>
				<td><c:out value="${triples.subject}"  /></td>
				<td><c:out value="${triples.predicate}"  /></td>
				<td><c:out value="${triples.object}"  /></td>
			</tr>
			</c:forEach>
		</tbody>
		</table>



	</body>
</html>






