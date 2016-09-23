<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<link rel="shortcut icon" href="assets/img/ico.png" >
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" href="assets/css/bootstrap.css">
	<link rel="stylesheet" href="assets/css/style.css">
	<link rel="stylesheet" href="assets/css/bootstrap-responsive.css">
	<link rel="stylesheet" href="assets/css/bootstrap-table.css">
	<script src="assets/js/jquery.js"></script>
	<script src="assets/js/bootstrap.min.js"></script>
	<script src="assets/js/bootstrap-table.js"></script>
	<script src="assets/js/bootstrap-table-key-events.js"></script>
	<title>ProviderOne | Projetos</title>
</head>
<body>
	<c:import url="barra-menus.jsp"></c:import>
	<br /><br /><br />
	<div class="container">
		<legend>Lista de Projetos</legend>
		<table id="table"
				data-toggle="table"
               data-show-columns="true"
               data-show-toggle="true"
               data-show-pagination-switch="true"
               data-show-refresh="true"
               data-search="true"
               data-pagination="true"
               data-key-events="true">
            <thead>   
				<tr>
					<th>Id</th>
					<th>T�tulo do Projeto</th>
					<th>Nome do Cliente</th>
					<th>Data de Cria��o</th>
					<th>Respons�vel do Projeto</th>
					<th>Status</th>
					<th>A��es</th>
				</tr>
			</thead>
			<c:forEach var="projeto" items="${projetos}">
				<tr>
					<td>${projeto.id}</td>
					<td>${projeto.tituloProjeto}</td>
					<td>${projeto.nomeCliente}"</td>
					<td><f:formatDate value="${projeto.dataCriacao.time}"
							pattern="dd/MM/yyyy" /></td>
					<td>${projeto.nomeResponsavel}</td>
					<td>${projeto.status}</td>
					
					<td><a href="clienteEdit?id=${projeto.id}">Editar</a> | <a
						href="#">Excluir</a> <p> <a href="#">Nova tarefa</a></td>
				</tr>
			</c:forEach>
		</table>
		<br /><br /><br /><br /><br /><br /><br /><br />
		<legend></legend>
	</div>
	<c:import url="rodape.jsp"></c:import>
</body>
</html>