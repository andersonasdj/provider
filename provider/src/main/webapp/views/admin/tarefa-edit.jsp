<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>ProviderOne | Tarefa</title>
	<link rel="shortcut icon" href="assets/img/ico.png" >
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link rel="stylesheet" href="assets/css/style.css">
	<link rel="stylesheet" href="assets/css/bootstrap.css">
	<link rel="stylesheet" href="assets/css/bootstrap-responsive.css">
</head>
<body>
	<c:import url="barra-menus.jsp"></c:import>		
	<br/><br/><br/>
	<form action="atualizarTarefa" method="post" class="form-horizontal container">
		<fieldset>
			<legend></legend>			
			<h4 align="right">Nova tarefa - </h4>
			<input type="hidden" id="projeto.id" name="projeto.id" value="${tarefa.projeto.id}">
			<input type="hidden" id="idTarefa" name="idTarefa" value="${tarefa.idTarefa}">
			<div class="control-group">
				<label class="control-label">Descri��o da Tarefa</label>
				<div class="controls">
					<input id="descricaoTarefa" name="descricaoTarefa" type="text" 
						value="${tarefa.descricaoTarefa}" placeholder="Descri��o da Nova Tarefa" class="input-xlarge" required>
					<p class="help-block">* Campo Obrigat�rio</p>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Respons�vel pela tarefa</label>
				<div class="controls">
					<select class="selectpicker" id="responsavelTarefa"
						name="responsavelTarefa">
						<option>${tarefa.responsavelTarefa}</option>
						<c:forEach var="funcionario" items="${funcionarios}">
							<option>${funcionario.nome}</option>
						</c:forEach>
					</select>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Observa��es da Tarefa</label>
				<div class="controls">
					<input class="form-control" id="obs" name="obs" type="text" placeholder="Observa��es da tarefa"
						value="${tarefa.obs}" onkeyup="limite_textarea_obs(this.value)" class="input-xlarge">
						<span id="contObs">255</span> Restantes <br>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Status</label>
				<div class="controls">
					<select class="selectpicker" id="statusTarefa"
						name="statusTarefa">
						<option>${tarefa.statusTarefa}</option>
						<option>N�o iniciada</option>
						<option>Iniciada</option>
						<option>Finalizada</option>
					</select>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label"></label>
				<div class="controls">
					<button id="enviar" name="salvar" class="btn btn-success">Salvar <i class="fa fa-floppy-o fa-lg"></i></button>
					<a class="btn btn-primary" href="listarProjetos" role="button">Voltar <i class="fa fa-reply-all fa-lg"></i></a>
				</div>
			</div>
			<legend></legend>
		</fieldset>
	</form>
	<c:import url="rodape.jsp"></c:import>
</body>
	<script src="assets/js/jquery.min.js"></script>
	<script src="assets/js/jquery-ui.js"></script>
	<script src="assets/js/bootstrap.min.js"></script>
    <script type="text/javascript">
	    function limite_textarea_obs(valor) {
	        quant = 255;
	        total = valor.length;
	        if(total <= quant) {
	            resto = quant - total;
	            document.getElementById('contObs').innerHTML = resto;
	        } else {
	            document.getElementById('obs').value = valor.substr(0,quant);
	        }
	    }
    </script>
</html>