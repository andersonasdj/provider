package br.com.providerone.controller;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.providerone.dao.ClienteDao;
import br.com.providerone.dao.ComputadorDao;
import br.com.providerone.dao.EmailDao;
import br.com.providerone.dao.FuncionarioDao;
import br.com.providerone.dao.SolicitacaoDao;
import br.com.providerone.mail.JavaMailApp;
import br.com.providerone.modelo.Cliente;
import br.com.providerone.modelo.Email;
import br.com.providerone.modelo.Funcionario;
import br.com.providerone.modelo.Relatorio;
import br.com.providerone.modelo.Solicitacao;

@Controller
public class SolicitacaoController {

	@RequestMapping("/solicitacaoForm")
	public String solicitacaoForm(HttpSession session, Model model) {
		if (session.getAttribute("clienteLogado") != null) {
			return "cliente/solicitacao-form";
		} if (session.getAttribute("funcionarioLogado") != null) {
			FuncionarioDao daoFun = new FuncionarioDao();
			ClienteDao daoCli = new ClienteDao();
			model.addAttribute("funcionarios", daoFun.listaFuncionarioAtivo());
			model.addAttribute("clientes", daoCli.listaCliente());
			return "admin/solicitacao-form";
		} 
		
		if (session.getAttribute("tecnicoLogado") != null) {
			Funcionario funcionario = (Funcionario) session.getAttribute("tecnicoLogado");
			ClienteDao daoCli = new ClienteDao();
			model.addAttribute("funcionario", funcionario);
			model.addAttribute("clientes", daoCli.listaCliente());
			return "funcionario/solicitacao-form";
		} 
		
		else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("salvarSolicitacao")
	public String salvarSolicitacao(Solicitacao solicitacao, HttpSession session) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			solicitacao.setStatus("Aberto");
			Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
			solicitacao.setDataAbertura(Calendar.getInstance());
			solicitacao.setAbriuChamado(cliente.getNome());
			solicitacao.setFormaAbertura("Aberto pelo cliente");
			String log = solicitacao.geraLogSolicitacao(null, cliente);
			solicitacao.setAndamentoDoChamado(log);
			dao.salvaSolicitcao(solicitacao);
			return "redirect:home";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("salvarSolicitacaoAdmin")
	public String salvarSolicitacaoAdmin(Solicitacao solicitacao, String nomeDoCliente, String nomeDoFuncionario, HttpSession session) {
		if (session.getAttribute("funcionarioLogado") != null) {
			GravaSolicTecAdmin(solicitacao, nomeDoCliente, nomeDoFuncionario);
			return "redirect:solicitacoesAbertas";
		} if (session.getAttribute("tecnicoLogado") != null) {
			GravaSolicTecAdmin(solicitacao, nomeDoCliente, nomeDoFuncionario);
			return "redirect:solicitacoesTecnico";
		} else {
			return "redirect:login";
		}
	}

	private void GravaSolicTecAdmin(Solicitacao solicitacao,
			String nomeDoCliente, String nomeDoFuncionario) {
		ClienteDao daoCli = new ClienteDao();
		FuncionarioDao daoFun = new FuncionarioDao();
		Cliente clienteASalvar = daoCli.buscaNomeCliente(nomeDoCliente);
		Funcionario funcionarioASalvar = daoFun.buscaNomeFuncionario(nomeDoFuncionario);
		SolicitacaoDao dao = new SolicitacaoDao();
		solicitacao.setDataAbertura(Calendar.getInstance());
		String log = solicitacao.geraLogSolicitacao(funcionarioASalvar, clienteASalvar);//Em revisão
		solicitacao.setAndamentoDoChamado(log); //Em revisão
		if(solicitacao.getStatus().equals("Abrir")){
			//JavaMailApp mail = new JavaMailApp();
			solicitacao.setStatus("Aberto"); //Em revisão
			dao.salvaSolicitacaoAdmin(solicitacao, funcionarioASalvar, clienteASalvar);
			//mail.enviaEmail(clienteASalvar, solicitacao);
		}
		if(solicitacao.getStatus().equals("Agendar")){
			solicitacao.setStatus("Agendado"); //Em revisão
			dao.salvaSolicitacaoAdminAgendado(solicitacao, funcionarioASalvar, clienteASalvar);
		}
		if(solicitacao.getStatus().equals("Em andamento")){
			solicitacao.setStatus("Em andamento"); //Em revisão
			solicitacao.setDataAndamento(Calendar.getInstance());
			dao.salvaSolicitacaoAdmin(solicitacao, funcionarioASalvar, clienteASalvar);
		}
	}

	@RequestMapping("finalizarSolicitacao")
	public String finalizarSolicitacao(Solicitacao solicitacao,
			HttpSession session) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			solicitacao.setStatus("Finalizado");
			dao.salvaSolicitcao(solicitacao);
			return "redirect:home";
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/abertos")
	public String abertos(HttpSession session, Model model) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesAbertasPorId(cliente.getId()));
			
			return qtdSolicitacoesCliente(model, cliente);
		} else {
			return "redirect:login";
		}
	}

	private String qtdSolicitacoesCliente(Model model, Cliente cliente) {
		Long ab, and, age;
		SolicitacaoDao daoAberto = new SolicitacaoDao();
		ab = daoAberto.listaQtdSolicitacoesAbertasPorIdDoCliente(cliente.getId());
		model.addAttribute("qtdAberto", ab);
		
		SolicitacaoDao daoAgendadas = new SolicitacaoDao();
		age = daoAgendadas.listaQtdSolicitacoesAgendadasPorIdDoCliente(cliente.getId());
		model.addAttribute("qtdAgendado", age);
		
		SolicitacaoDao daoAndamento = new SolicitacaoDao();
		and = daoAndamento.listaQtdSolicitacoesAndamentoPorIdDoCliente(cliente.getId());
		model.addAttribute("qtdAndamento", and);
		
		model.addAttribute("qtdTotal", ab + age + and);	
		return "cliente/solicitacao-aberto";
	}
	
	@RequestMapping("/agendados")
	public String agendados(HttpSession session, Model model) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesAgendadasPorId(cliente.getId()));
			
			return qtdSolicitacoesCliente(model, cliente);
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/andamento")
	public String andamento(HttpSession session, Model model) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesAndamentoPorId(cliente.getId()));
			
			return qtdSolicitacoesCliente(model, cliente);
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/solicitacoesTecnico")
	public String abertosTecnico(HttpSession session, Model model) {
		if (session.getAttribute("tecnicoLogado") != null) {
			
			SolicitacaoDao dao = new SolicitacaoDao();
			Funcionario funcionario = (Funcionario) session.getAttribute("tecnicoLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesAbertasPorIdDoTecnico(funcionario.getId()));
			
			return qtdSolicitacoesTecnico(model, funcionario);
		} else {
			return "redirect:login";
		}
	}

	private String qtdSolicitacoesTecnico(Model model, Funcionario funcionario) {
		SolicitacaoDao daoAberto = new SolicitacaoDao();
		model.addAttribute("qtdAberto", daoAberto.listaQtdSolicitacoesAbertasPorIdDoTecnico(funcionario.getId()));
		SolicitacaoDao daoAgendadas = new SolicitacaoDao();
		model.addAttribute("qtdAgendado", daoAgendadas.listaQtdSolicitacoesAgendadasPorIdDoTecnico(funcionario.getId()));
		SolicitacaoDao daoAndamento = new SolicitacaoDao();
		model.addAttribute("qtdAndamento", daoAndamento.listaQtdSolicitacoesEmAndamentoPorIdDoTecnico(funcionario.getId()));
		SolicitacaoDao daoAguardando = new SolicitacaoDao();
		model.addAttribute("qtdAguardando", daoAguardando.listaQtdSolicitacoesAguardandoPorIdDoTecnico(funcionario.getId()));	
		return "funcionario/solicitacao-aberto";
	}
		
	@RequestMapping("/solicitacoesAgendadosTecnico")
	public String agendadosTecnico(HttpSession session, Model model) {
		if (session.getAttribute("tecnicoLogado") != null) {
			
			SolicitacaoDao dao = new SolicitacaoDao();
			Funcionario funcionario = (Funcionario) session.getAttribute("tecnicoLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesAgendadasPorIdDoTecnico(funcionario.getId()));
			
			return qtdSolicitacoesTecnico(model, funcionario);
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/solicitacoesAndamentoTecnico")
	public String andamentoTecnico(HttpSession session, Model model) {
		if (session.getAttribute("tecnicoLogado") != null) {
			
			SolicitacaoDao dao = new SolicitacaoDao();
			Funcionario funcionario = (Funcionario) session.getAttribute("tecnicoLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesAndamentoPorIdDoTecnico(funcionario.getId()));
			
			return qtdSolicitacoesTecnico(model, funcionario);
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/solicitacoesAguardandoTecnico")
	public String aguardandoTecnico(HttpSession session, Model model) {
		if (session.getAttribute("tecnicoLogado") != null) {
			
			SolicitacaoDao dao = new SolicitacaoDao();
			Funcionario funcionario = (Funcionario) session.getAttribute("tecnicoLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesAguardandoPorIdDoTecnico(funcionario.getId()));
			
			return qtdSolicitacoesTecnico(model, funcionario);
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/relatorio")
	public String relatorio(HttpSession session, Model model) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
			model.addAttribute("solicitacoes",dao.listaSolicitacoesPorId(cliente.getId()));
			return "cliente/solicitacao-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioGeral")
	public String relatorioGeral(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaTodasSolicitacoes());
			
			Long ab, and, age, agua;
			
			SolicitacaoDao daoAbertas = new SolicitacaoDao();
			ab = daoAbertas.listaQtdSolicitacoesAbertas();
			model.addAttribute("qtdAberto",ab );
			
			SolicitacaoDao daoAgendadas = new SolicitacaoDao();
			age = daoAgendadas.listaQtdSolicitacoesAgendadas();
			model.addAttribute("qtdAgendado",age );
			
			SolicitacaoDao daoAndamento = new SolicitacaoDao();
			and = daoAndamento.listaQtdSolicitacoesEmAndamento();
			model.addAttribute("qtdAndamento", and);
			
			SolicitacaoDao daoAguardando = new SolicitacaoDao();
			agua = daoAguardando.listaQtdSolicitacoesAguardando();
			model.addAttribute("qtdAguardando", agua);
			model.addAttribute("qtdTotal", ab + age + and + agua);	
			
			return "admin/solicitacao-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioFinalizadas")
	public String relatorioFinalizadas(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaTodasSolicitacoesFinalizadas());
			return "admin/solicitacao-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioTecnico")
	public String relatorioTecnico(HttpSession session, Model model) {
		if (session.getAttribute("tecnicoLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaTodasSolicitacoes());
			return "funcionario/solicitacao-relatorio";
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/solicitacoesAbertas")
	public String solicitacoesAbertas(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesAbertas());
			
			return qtdSolicitacoesFuncionario(model);
		} else {
			return "redirect:login";
		}
	}

	private String qtdSolicitacoesFuncionario(Model model) {
		Long ab, and, age, agua;
		
		SolicitacaoDao daoAbertas = new SolicitacaoDao();
		ab = daoAbertas.listaQtdSolicitacoesAbertas();
		model.addAttribute("qtdAberto",ab );
		
		SolicitacaoDao daoAgendadas = new SolicitacaoDao();
		age = daoAgendadas.listaQtdSolicitacoesAgendadas();
		model.addAttribute("qtdAgendado",age );
		
		SolicitacaoDao daoAndamento = new SolicitacaoDao();
		and = daoAndamento.listaQtdSolicitacoesEmAndamento();
		model.addAttribute("qtdAndamento", and);
		
		SolicitacaoDao daoAguardando = new SolicitacaoDao();
		agua = daoAguardando.listaQtdSolicitacoesAguardando();
		model.addAttribute("qtdAguardando", agua);
		model.addAttribute("qtdTotal", ab + age + and + agua);	
		return "admin/solicitacao-aberto";
	}
	
	@RequestMapping("/solicitacoesAgendadas")
	public String solicitacoesAgendadas(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesAgendadas());
			
			return qtdSolicitacoesFuncionario(model);
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("solicitacoesAndamento")
	public String solicitacoesAndamento(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {		
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesAndamento());
			
			return qtdSolicitacoesFuncionario(model);
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("solicitacoesAguardando")
	public String solicitacoesAguardando(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {	
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesAguardando());
			
			return qtdSolicitacoesFuncionario(model);
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/solicitacaoEdit")
	public String funcionarioEdit(Long id, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			FuncionarioDao daoFun = new FuncionarioDao();
			Solicitacao solicitacaoEditada = new Solicitacao();
			solicitacaoEditada = dao.buscaSolicitacaoId(id);
			model.addAttribute("solicitacao", solicitacaoEditada);
			model.addAttribute("funcionario", daoFun.listaFuncionarioAtivo());
			return "admin/solicitacao-edit";
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/solicitacaoEditCliente")
	public String funcionarioEditCliente(Long id, HttpSession session,
			Model model) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Solicitacao solicitacaoEditada = new Solicitacao();
			solicitacaoEditada = dao.buscaSolicitacaoId(id);
			Cliente cliente =  (Cliente) session.getAttribute("clienteLogado");
			/* Limita usuário na alteração da URL passando qualquer ID validando somente 
			 * ID correto ao cliente
			 */
			if(solicitacaoEditada.getCliente().getNome().equals(cliente.getNome())){
				model.addAttribute("solicitacao", solicitacaoEditada);
				return "cliente/solicitacao-edit";
			}
			return "redirect:abertos";
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/solicitacaoEditFuncionario")
	public String solicitacaoEditFuncionario(Long id, HttpSession session,
			Model model) {
		if (session.getAttribute("tecnicoLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Solicitacao solicitacaoEditada = new Solicitacao();
			solicitacaoEditada = dao.buscaSolicitacaoId(id);
			model.addAttribute("solicitacao", solicitacaoEditada);
			return "funcionario/solicitacao-edit";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/visualizaSolicitacao")
	public String visualuzaSolicitacaso(Long id, HttpSession session,
			Model model) {
		if (session.getAttribute("tecnicoLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Solicitacao solicitacaoEditada = new Solicitacao();
			solicitacaoEditada = dao.buscaSolicitacaoId(id);
			model.addAttribute("solicitacao", solicitacaoEditada);
			return "funcionario/solicitacao-ver";
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/atualizarSolicitacao")
	public String atualizarSolicitacao(Solicitacao solicitacao, String nomeDoFuncionario, String funcionarioLogado, HttpSession session) {
		
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			FuncionarioDao daoFun = new FuncionarioDao();
			Funcionario funcionarioASalvar = daoFun.buscaNomeFuncionario(nomeDoFuncionario);
			
			if (solicitacao.getStatus().equals("Finalizar")) {
				dao.finalizaSolicitacao(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesAbertas";
			}
			if (solicitacao.getStatus().equals("Finalizado")) {
				dao.atualizarSolicitacaoFinalizada(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesAbertas";
			}
			if (solicitacao.getStatus().equals("Aberto")) {
				dao.atualizarSolicitacao(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesAbertas";
			}
			if (solicitacao.getStatus().equals("Agendado")) {
				dao.agendarSolicitacao(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesAbertas";
			} 
			if (solicitacao.getStatus().equals("Em andamento")) {
				dao.solicitacaoEmAndamento(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesAbertas";
			}
			if (solicitacao.getStatus().equals("Aguardando usuario")) {
				dao.atualizarSolicitacao(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesAbertas";
			}else {
				return "redirect:solicitacoesAbertas";
			}
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/atualizarSolicitacaoCliente")
	public String atualizarSolicitacaoCLiente(Solicitacao solicitacao,
			HttpSession session) {
		if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			dao.atualizarSolicitacaoCliente(solicitacao);
			return "redirect:abertos";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/atualizarSolicitacaoFuncionario")
	public String atualizarSolicitacaoFuncionario(Solicitacao solicitacao, String nomeDoFuncionario, String funcionarioLogado, HttpSession session) {
		
		if (session.getAttribute("tecnicoLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			FuncionarioDao daoFun = new FuncionarioDao();
			Funcionario funcionarioASalvar = daoFun.buscaNomeFuncionario(nomeDoFuncionario);
			
			if (solicitacao.getStatus().equals("Finalizar")) {
				dao.finalizaSolicitacao(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesTecnico";
			}
			if (solicitacao.getStatus().equals("Aberto")) {
				dao.atualizarSolicitacao(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesTecnico";
			}
			if (solicitacao.getStatus().equals("Em andamento")) {
				dao.solicitacaoEmAndamento(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesTecnico";
			}
			if (solicitacao.getStatus().equals("Agendado")) {
				dao.solicitacaoAgendado(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesTecnico";
			}
			if (solicitacao.getStatus().equals("Aguardando usuario")) {
				dao.atualizarSolicitacao(solicitacao, funcionarioASalvar, funcionarioLogado);
				return "redirect:solicitacoesTecnico";
			} else {
				return "redirect:solicitacoesTecnico";
			}
		} else {
			return "redirect:login";
		}
	}

	@RequestMapping("/removeSolicitacao")
	public String removeSolicitacao(Long id, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			dao.excluiSolicitacaoPorId(id);
			return "redirect:solicitacoesAbertas";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("enviaEmail")
	public String enviaEmail(Long id, String destinatario,
			HttpSession session) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			Solicitacao solicitacao = dao.salvaSolicitacaoEmail(id);
			EmailDao emailDao = new EmailDao();
			Email emailConfig = emailDao.listaEmailConfigAbertura();
			JavaMailApp mail = new JavaMailApp(emailConfig);
			mail.enviaEmail(solicitacao.getCliente(), solicitacao, destinatario);
			return "redirect:solicitacoesAbertas";
		} else {
			return "redirect:solicitacoesAbertas";
		}
	}
	
	@RequestMapping("/solicitacoesResumo")
	public String solicitacoesResumo(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaTodasSolicitacoes());
			return "admin/solicitacao-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioSelect")
	public String relatorioSelect(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			ClienteDao daoCli = new ClienteDao();
			model.addAttribute("clientes", daoCli.listaCliente());
			return "admin/cliente-gera-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioOpcoes")
	public String relatorioOpcoes(HttpSession session, String nomeDoCliente, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			model.addAttribute("nomeDoCliente", nomeDoCliente);	
			ClienteDao daoCli = new ClienteDao();
			Cliente clienteEncontrado = daoCli.buscaNomeCliente(nomeDoCliente);

			SolicitacaoDao daoSolicitacao = new SolicitacaoDao();
			model.addAttribute("qtdSolicitacoes", daoSolicitacao.listaQtdSolicitacoesPorIdDoCliente(clienteEncontrado.getId()));	
		
			ComputadorDao daoComputador = new ComputadorDao();
			model.addAttribute("qtdComputador", daoComputador.listaQtdComputadoresPorIdDoCliente(clienteEncontrado.getId()));
			return "admin/cliente-opcoes-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioPorCliente")
	public String relatorioPorCliente(HttpSession session, String nomeDoCliente, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			ClienteDao daoCli = new ClienteDao();
			Cliente clienteSelecionado = daoCli.buscaNomeCliente(nomeDoCliente);
			
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorId(clienteSelecionado.getId()));
			return "admin/cliente-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioSelectTec")
	public String relatorioSelectTec(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			FuncionarioDao daoFun = new FuncionarioDao();
			model.addAttribute("funcionarios", daoFun.listaFuncionario());
			return "admin/funcionario-gera-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioSelectData") 
	public String relatorioSelectData(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			FuncionarioDao daoFun = new FuncionarioDao();
			model.addAttribute("funcionarios", daoFun.listaFuncionario());
			return "admin/data-gera-relatorio";
		} else {
			return "redirect:login";
		}
	}
	//**************************************
	@RequestMapping("/relatorioPorData")
	public String relatorioPorData(HttpSession session, String dataInicio,  Model model) throws ParseException {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			
			System.out.println(dataInicio);
			
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = (Date)formatter.parse(dataInicio);
			
			System.out.println(date);
			
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorDataMedia(date, date));
			//System.out.println(dao.listaSolicitacoesPorData(date, date));
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioPorFuncionario")
	public String relatorioPorFuncionario(HttpSession session, String nomeDoFuncionario, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao dao = new SolicitacaoDao();
			FuncionarioDao daoFun = new FuncionarioDao();
			Funcionario funiconarioSelecionado = daoFun.buscaNomeFuncionario(nomeDoFuncionario);
			
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorIdTec(funiconarioSelecionado.getId()));
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/logDeSolicitacao")
	public String solicitacaoLog(HttpSession session,Long id, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			SolicitacaoDao daoSolicitacao = new SolicitacaoDao();
			
			model.addAttribute("solicitacao", daoSolicitacao.buscaSolicitacaoId(id));
			return "admin/solicitacao-log";
		} if (session.getAttribute("clienteLogado") != null) {
			SolicitacaoDao daoSolicitacao = new SolicitacaoDao();
			Cliente cliente = (Cliente) session.getAttribute("clienteLogado");
			Solicitacao solicitacaoExibida = daoSolicitacao.buscaSolicitacaoId(id);
			
			if(solicitacaoExibida.getCliente().getNome().equals(cliente.getNome())){
				model.addAttribute("solicitacao", solicitacaoExibida );
				return "cliente/solicitacao-log";				
			}
			else {
				return "redirect:abertos";
			}
		} 
			
		else {
			return "redirect:login";
		}
	}
	
	//######################################################################
	
	@RequestMapping("/relatorioOp") 
	public String relatorioOp(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar hoje = Calendar.getInstance();
			hoje.set(Calendar.HOUR_OF_DAY, 0);
			hoje.set(Calendar.MINUTE, 0);
			hoje.set(Calendar.SECOND, 0);
			
			//####################################################################################
			
			FuncionarioDao daoFun = new FuncionarioDao();
			
			List<Funcionario> funcionarios = daoFun.listaFuncionarioAtivo();
			List<Relatorio> relatorios = new ArrayList<Relatorio>();
			
			for(int i=0; i < funcionarios.size(); i++){
				SolicitacaoDao daoSolAberto = new SolicitacaoDao();
				SolicitacaoDao daoSolAndamento = new SolicitacaoDao();
				SolicitacaoDao daoSolAgendado = new SolicitacaoDao();
				SolicitacaoDao daoSolAguardando = new SolicitacaoDao();
				
				if (funcionarios.get(i).getId()!= null){
					
					Relatorio relaTemp = new Relatorio();
					relaTemp.setId(funcionarios.get(i).getId());
					relaTemp.setNome(funcionarios.get(i).getNome());
					relaTemp.setQtdAberto(daoSolAberto.listaQtdSolicitacoesAbertasPorIdDoTecnico(funcionarios.get(i).getId()));
					relaTemp.setQtdAndamento(daoSolAndamento.listaQtdSolicitacoesEmAndamentoPorIdDoTecnico(funcionarios.get(i).getId()));
					relaTemp.setQtdAgendado(daoSolAgendado.listaQtdSolicitacoesAgendadasPorIdDoTecnico(funcionarios.get(i).getId()));
					relaTemp.setQtdAguardando(daoSolAguardando.listaQtdSolicitacoesAguardandoPorIdDoTecnico(funcionarios.get(i).getId()));
					relaTemp.setQtdTotal(relaTemp.getQtdAberto() + relaTemp.getQtdAgendado() + relaTemp.getQtdAguardando() + relaTemp.getQtdAndamento());
					relatorios.add(relaTemp);
				}
			}
			
			//####################################################################################
			SolicitacaoDao daoHoje = new SolicitacaoDao();
			model.addAttribute("solicitacoes", daoHoje.listaSolicitacoesPorData(hoje).size());
			SolicitacaoDao daoFinalizados = new SolicitacaoDao();
			model.addAttribute("finalizados", daoFinalizados.listaSolicitacoesPorDataFinalizacao(hoje).size());
			model.addAttribute("relatorios", relatorios);
			
			return "admin/relatorio-op";
		} else {
			return "redirect:login";
		}
	}
	
	
	@RequestMapping("/relatorioHoje")
	public String relatorioHoje(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar hoje = Calendar.getInstance();
			hoje.set(Calendar.HOUR_OF_DAY, 0);
			hoje.set(Calendar.MINUTE, 0);
			hoje.set(Calendar.SECOND, 0);
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorData(hoje));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/finalizadoHoje")
	public String finalizadoHoje(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar hoje = Calendar.getInstance();
			hoje.set(Calendar.HOUR_OF_DAY, 0);
			hoje.set(Calendar.MINUTE, 0);
			hoje.set(Calendar.SECOND, 0);
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorDataFinalizacao(hoje));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("gerarRelatorioPalavra")
	public String gerarRelatorioPalavra(String assunto, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorAssunto(assunto));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioPalavra")
	public String relatorioPalavra(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			return "admin/relatorio-assunto";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioPeriodoFechamento")
	public String relatorioPeriodoFechamento(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			return "admin/relatorio-periodo-f";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioPeriodoAbertura")
	public String relatorioPeriodoAbertura(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			return "admin/relatorio-periodo-a";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("gerarRelatorioPeriodoFechamento")
	public String gerarRelatorioPeriodoFechamento(String dataInicio, String dataFim, String assunto, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar dataInicioConv, dataFimConv;
			dataInicioConv = Calendar.getInstance();
			dataFimConv = Calendar.getInstance();
			
			try {
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				dataInicioConv.setTime(sdf.parse(dataInicio));
				dataFimConv.setTime(sdf.parse(dataFim));
				
				dataInicioConv.set(Calendar.HOUR_OF_DAY, 0);
				dataInicioConv.set(Calendar.MINUTE, 0);
				dataInicioConv.set(Calendar.SECOND, 0);
				
				dataFimConv.set(Calendar.HOUR_OF_DAY, 0);
				dataFimConv.set(Calendar.MINUTE, 0);
				dataFimConv.set(Calendar.SECOND, 0);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorPeriodoFechamento(dataInicioConv, dataFimConv));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("gerarRelatorioPeriodoAbertura")
	public String gerarRelatorioPeriodoAbertura(String dataInicio, String dataFim, String assunto, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar dataInicioConv, dataFimConv;
			dataInicioConv = Calendar.getInstance();
			dataFimConv = Calendar.getInstance();
			
			try {
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				dataInicioConv.setTime(sdf.parse(dataInicio));
				dataFimConv.setTime(sdf.parse(dataFim));
				
				dataInicioConv.set(Calendar.HOUR_OF_DAY, 0);
				dataInicioConv.set(Calendar.MINUTE, 0);
				dataInicioConv.set(Calendar.SECOND, 0);
				
				dataFimConv.set(Calendar.HOUR_OF_DAY, 0);
				dataFimConv.set(Calendar.MINUTE, 0);
				dataFimConv.set(Calendar.SECOND, 0);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorPeriodoAbertura(dataInicioConv, dataFimConv));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioAgendamento")
	public String relatorioAgendamento(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			return "admin/relatorio-agendamento";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("gerarRelatorioAgendamento")
	public String gerarRelatorioAgendamento(String data, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar dataInicioConv;
			dataInicioConv = Calendar.getInstance();
			
			try {
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				dataInicioConv.setTime(sdf.parse(data));
				dataInicioConv.set(Calendar.HOUR_OF_DAY, 0);
				dataInicioConv.set(Calendar.MINUTE, 0);
				dataInicioConv.set(Calendar.SECOND, 0);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorAgendamento(dataInicioConv));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioId")
	public String relatorioId(HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			return "admin/relatorio-id";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("gerarRelatorioId")
	public String gerarRelatorioId(Long id, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			System.out.println(id);
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorIdSolicitacao(id));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioPeriodoAberturaCliente")
	public String relatorioPeriodoAberturaCliente(String nomeDoCliente, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			model.addAttribute("nomeDoCliente", nomeDoCliente);
			return "admin/relatorio-periodo-a-cli";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("/relatorioPeriodoFechamentoCliente")
	public String relatorioPeriodoFechamentoCliente(String nomeDoCliente, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			model.addAttribute("nomeDoCliente", nomeDoCliente);
			return "admin/relatorio-periodo-f-cli";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("gerarRelatorioPeriodoAberturaCliente")
	public String gerarRelatorioPeriodoAberturaCliente(String dataInicio, String dataFim, String nomeDoCliente, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar dataInicioConv, dataFimConv;
			dataInicioConv = Calendar.getInstance();
			dataFimConv = Calendar.getInstance();
			
			try {
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				dataInicioConv.setTime(sdf.parse(dataInicio));
				dataFimConv.setTime(sdf.parse(dataFim));
				
				dataInicioConv.set(Calendar.HOUR_OF_DAY, 0);
				dataInicioConv.set(Calendar.MINUTE, 0);
				dataInicioConv.set(Calendar.SECOND, 0);
				
				dataFimConv.set(Calendar.HOUR_OF_DAY, 0);
				dataFimConv.set(Calendar.MINUTE, 0);
				dataFimConv.set(Calendar.SECOND, 0);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorPeriodoAberturaCliente(dataInicioConv, dataFimConv, nomeDoCliente));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
	
	@RequestMapping("gerarRelatorioPeriodoFechamentoCliente")
	public String gerarRelatorioPeriodoFechamentoCliente(String dataInicio, String dataFim, String nomeDoCliente, HttpSession session, Model model) {
		if (session.getAttribute("funcionarioLogado") != null) {
			
			Calendar dataInicioConv, dataFimConv;
			dataInicioConv = Calendar.getInstance();
			dataFimConv = Calendar.getInstance();
			
			try {
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				dataInicioConv.setTime(sdf.parse(dataInicio));
				dataFimConv.setTime(sdf.parse(dataFim));
				
				dataInicioConv.set(Calendar.HOUR_OF_DAY, 0);
				dataInicioConv.set(Calendar.MINUTE, 0);
				dataInicioConv.set(Calendar.SECOND, 0);
				
				dataFimConv.set(Calendar.HOUR_OF_DAY, 0);
				dataFimConv.set(Calendar.MINUTE, 0);
				dataFimConv.set(Calendar.SECOND, 0);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			SolicitacaoDao dao = new SolicitacaoDao();
			model.addAttribute("solicitacoes", dao.listaSolicitacoesPorPeriodoFechamentoCliente(dataInicioConv, dataFimConv, nomeDoCliente));
			
			return "admin/funcionario-relatorio";
		} else {
			return "redirect:login";
		}
	}
}