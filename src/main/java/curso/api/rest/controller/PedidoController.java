package curso.api.rest.controller;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.model.Pedido;
import curso.api.rest.model.UserChart;
import curso.api.rest.model.UserReport;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.ItemPedidoRepository;
import curso.api.rest.repositoy.PedidoRepository;
import curso.api.rest.repositoy.ProdutoRepository;
import curso.api.rest.repositoy.TelefoneRepository;
import curso.api.rest.repositoy.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsSercice;
import curso.api.rest.service.ServiceRelatorio;

@RestController /* Arquitetura REST */
@RequestMapping(value = "/pedido")
public class PedidoController {

	@Autowired /* de fosse CDI seria @Inject */
	
     private PedidoRepository pedidoRepository;
	@Autowired
	private ItemPedidoRepository itempedidoRepository;

	@Autowired
	private ImplementacaoUserDetailsSercice implementacaoUserDetailsSercice;
	
	@Autowired
	private ServiceRelatorio serviceRelatorio;
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	


	/* Serviço RESTful */
	@GetMapping(value = "/{id}", produces = "application/json")
	@CachePut("cacheuser")
	public ResponseEntity<Pedido> init(@PathVariable(value = "id") Long id) {

		Optional<Pedido> pedido = pedidoRepository.findById(id);

		return new ResponseEntity<Pedido>(pedido.get(), HttpStatus.OK);
	}

	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete(@PathVariable("id") Long id) {

		pedidoRepository.deleteById(id);

		return "ok";
	}

	@DeleteMapping(value = "/{id}/venda", produces = "application/text")
	public String deletevenda(@PathVariable("id") Long id) {

	  pedidoRepository.deleteById(id);

		return "ok";
	}

	/*
	 * Vamos supor que o carregamento de usuário seja um processo lento e queremos
	 * controlar ele com cache para agilizar o processo
	 */
	@GetMapping(value = "/", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Pedido>> pedido() throws InterruptedException {

		PageRequest page = PageRequest.of(0, 5, Sort.by("nome"));

		Page<Pedido> list = pedidoRepository.findAll( page);

		return new ResponseEntity<Page<Pedido>>(list, HttpStatus.OK);
	}

	@GetMapping(value = "/page/{pagina}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Pedido>> pedidoPagina(@PathVariable("pagina") int pagina) throws InterruptedException {

		PageRequest page = PageRequest.of(pagina, 5, Sort.by("nome"));

		Page<Pedido> list = pedidoRepository.findAll(page);

		return new ResponseEntity<Page<Pedido>>(list, HttpStatus.OK);
	}

	/* END-POINT consulta de usuário por nome */
	@GetMapping(value = "/pedidoPorNome/{nome}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Pedido>> pedidoPorNome(@PathVariable("nome") String nome) throws InterruptedException {

		PageRequest pageRequest = null;
		Page<Pedido> list = null;

		if (nome == null || (nome != null && nome.trim().isEmpty())
				|| nome.equalsIgnoreCase("undefined")) {/* Não informou nome */

			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = pedidoRepository.findAll( pageRequest);
		} else {
			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = pedidoRepository.findPedidoByNamePage(nome, pageRequest);
		}

		return new ResponseEntity<Page<Pedido>>(list, HttpStatus.OK);
	}

	/* END-POINT consulta de usuário por nome */
	@GetMapping(value = "/usuarioPorNome/{nome}/page/{page}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Pedido>> pedidoPorNomePage(@PathVariable("nome") String nome,
			@PathVariable("page") int page) throws InterruptedException {

		PageRequest pageRequest = null;
		Page<Pedido> list = null;

		if (nome == null || (nome != null && nome.trim().isEmpty())
				|| nome.equalsIgnoreCase("undefined")) {/* Não informou nome */

			pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
			list = pedidoRepository.findAll(pageRequest);
		} else {
			pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
			list = pedidoRepository.findPedidoByNamePage(nome, pageRequest);
		}

		return new ResponseEntity<Page<Pedido>>(list, HttpStatus.OK);
	}

	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Pedido> cadastrar(@RequestBody @Validated Pedido pedido) {

		for (int pos = 0; pos < pedido.getItemPedido().size(); pos++) {
			pedido.getItemPedido().get(pos).setPedido(pedido);
		}

	//	String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		//pedido.setSenha(senhacriptografada);
		Pedido pedidoSalvo = pedidoRepository.save(pedido);

		implementacaoUserDetailsSercice.insereAcessoPadrao(pedidoSalvo.getId());

		return new ResponseEntity<Pedido>(pedidoSalvo, HttpStatus.OK);

	}

	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Pedido> atualizar(@RequestBody Pedido pedido) {

		/* outras rotinas antes de atualizar */

		for (int pos = 0; pos < pedido.getItemPedido().size(); pos++) {
			pedido.getItemPedido().get(pos).setPedido(pedido);
		}

		//Usuario userTemporario = PedidoRepository.findById(pedido.getId()).get();

		//if (!userTemporario.getSenha().equals(usuario.getSenha())) { /* Senhas diferentes */
		//	String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		//	usuario.setSenha(senhacriptografada);
		//}

		Pedido pedidoSalvo = pedidoRepository.save(pedido);

		return new ResponseEntity<Pedido>(pedidoSalvo, HttpStatus.OK);

	}

	@PutMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity updateVenda(@PathVariable Long iduser, @PathVariable Long idvenda) {
		/* outras rotinas antes de atualizar */

		// Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity("Venda atualzada", HttpStatus.OK);

	}
	
	

	@PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity cadastrarvenda(@PathVariable Long iduser, @PathVariable Long idvenda) {

		/* Aqui seria o processo de venda */
		// Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity("id user :" + iduser + " idvenda :" + idvenda, HttpStatus.OK);

	}

	@DeleteMapping(value = "/removerTelefone/{id}", produces = "application/text")
	public String deleteItemPedido(@PathVariable("id") Long id) {

		itempedidoRepository.deleteById(id);

		return "ok";
	}
	
	@GetMapping(value="/relatorio", produces = "application/text")
	public ResponseEntity<String> downloadRelatorio(HttpServletRequest request) throws Exception {
		byte[] pdf = serviceRelatorio.gerarRelatorio("relatorio-usuario", new HashMap(), 
				request.getServletContext());
		
		String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
		
		return new ResponseEntity<String>(base64Pdf, HttpStatus.OK);
		
	}
	
	
	
	@PostMapping(value="/relatorio/", produces = "application/text")
	public ResponseEntity<String> downloadRelatorioParam(HttpServletRequest request, 
			@RequestBody UserReport userReport) throws Exception {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		SimpleDateFormat dateFormatParam = new SimpleDateFormat("yyyy-MM-dd");
		
		String dataInicio =  dateFormatParam.format(dateFormat.parse(userReport.getDataInicio()));
		
		String dataFim =  dateFormatParam.format(dateFormat.parse(userReport.getDataFim()));
		
		Map<String,Object> params = new HashMap<String, Object>();
		
		params.put("DATA_INICIO", dataInicio);
		params.put("DATA_FIM", dataFim);
		
		byte[] pdf = serviceRelatorio.gerarRelatorio("relatorio-usuario-param", params,
				request.getServletContext());
		
		String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
		
		return new ResponseEntity<String>(base64Pdf, HttpStatus.OK);
		
	}
	
	@GetMapping(value= "/grafico", produces = "application/json")
	public ResponseEntity<UserChart> grafico(){
		
		UserChart userChart = new UserChart();
		
		List<String> resultado = jdbcTemplate.queryForList("select array_agg(nome) from usuario where salario > 0 and nome <> '' union all select  cast(array_agg(salario) as character varying[]) from usuario where salario > 0 and nome <> ''", String.class);
		
		if (!resultado.isEmpty()) {
			String nomes = resultado.get(0).replaceAll("\\{", "").replaceAll("\\}", "");
			String salario = resultado.get(1).replaceAll("\\{", "").replaceAll("\\}", "");
			
			userChart.setNome(nomes);
			userChart.setSalario(salario);
		}
		
		return new ResponseEntity<UserChart>(userChart, HttpStatus.OK);
		
	}
		

}
