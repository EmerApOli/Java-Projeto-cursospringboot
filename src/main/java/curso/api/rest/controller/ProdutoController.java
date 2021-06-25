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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import curso.angular.dao.DaoImplementacao;
import curso.angular.dao.DaoInterface;

import curso.api.rest.model.Produto;
import curso.api.rest.model.UserChart;
import curso.api.rest.model.UserReport;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.FornecedorRepository;
import curso.api.rest.repositoy.ProdutoRepository;
import curso.api.rest.repositoy.TelefoneRepository;
import curso.api.rest.repositoy.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsSercice;
import curso.api.rest.service.ServiceRelatorio;



//import com.google.gson.Gson;



@RestController
@RequestMapping(value = "/produto")
public class ProdutoController  implements
DaoInterface<Produto> {

	//@Autowired /* de fosse CDI seria @Inject */
	//private UsuarioRepository usuarioRepository;
	
	

	@Autowired /* de fosse CDI seria @Inject */
	private ProdutoRepository produtoRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private ImplementacaoUserDetailsSercice implementacaoUserDetailsSercice;
	
	@Autowired
	private ServiceRelatorio serviceRelatorio;
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/* Serviço RESTful */
	@GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
	public ResponseEntity<Produto> relatorio(@PathVariable(value = "id") Long id,
			@PathVariable(value = "venda") Long venda) {

		Optional<Produto> produto = produtoRepository.findById(id);

		/* o retorno seria um relatorio */
		return new ResponseEntity<Produto>(produto.get(), HttpStatus.OK);
	}

	/* Serviço RESTful */
	@GetMapping(value = "/{id}", produces = "application/json")
	@CachePut("cacheuser")
	public ResponseEntity<Produto> init(@PathVariable(value = "id") Long id) {

		Optional<Produto> produto = produtoRepository.findById(id);

		return new ResponseEntity<Produto>(produto.get(), HttpStatus.OK);
	}

	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete(@PathVariable("id") Long id) {

		produtoRepository.deleteById(id);

		return "ok";
	}

	///@DeleteMapping(value = "/{id}/venda", produces = "application/text")
	//public String deletevenda(@PathVariable("id") Long id) {

	//	produtoRepository.deleteById(id);

	//	return "ok";
	//}

	/*
	 * Vamos supor que o carregamento de usuário seja um processo lento e queremos
	 * controlar ele com cache para agilizar o processo
	 */
	@GetMapping(value = "/", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Produto>> produto() throws InterruptedException {

		PageRequest page = PageRequest.of(0, 5, Sort.by("nome"));

		Page<Produto> list = produtoRepository.findAll(page);

		return new ResponseEntity<Page<Produto>>(list, HttpStatus.OK);
	}

	@GetMapping(value = "/page/{pagina}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Produto>> usuarioPagina(@PathVariable("pagina") int pagina) throws InterruptedException {

		PageRequest page = PageRequest.of(pagina, 5, Sort.by("nome"));

		Page<Produto> list = produtoRepository.findAll(page);

		return new ResponseEntity<Page<Produto>>(list, HttpStatus.OK);
	}

	/* END-POINT consulta de usuário por nome */
	@GetMapping(value = "/produtoPorNome/{nome}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Produto>> usuarioPorNome(@PathVariable("nome") String nome) throws InterruptedException {

		PageRequest pageRequest = null;
		Page<Produto> list = null;

		if (nome == null || (nome != null && nome.trim().isEmpty())
				|| nome.equalsIgnoreCase("undefined")) {/* Não informou nome */

			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = produtoRepository.findAll(pageRequest);
		} else {
			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = produtoRepository.findProdutoByNamePage(nome, pageRequest);
		}

		return new ResponseEntity<Page<Produto>>(list, HttpStatus.OK);
	}

	/* END-POINT consulta de usuário por nome */
	@GetMapping(value = "/produtoPorNome/{nome}/page/{page}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Produto>> usuarioPorNomePage(@PathVariable("nome") String nome,
			@PathVariable("page") int page) throws InterruptedException {

		PageRequest pageRequest = null;
		Page<Produto> list = null;

		if (nome == null || (nome != null && nome.trim().isEmpty())
				|| nome.equalsIgnoreCase("undefined")) {/* Não informou nome */

			pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
			list = produtoRepository.findAll(pageRequest);
		} else {
			pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
			list = produtoRepository.findProdutoByNamePage(nome, pageRequest);
		}

		return new ResponseEntity<Page<Produto>>(list, HttpStatus.OK);
	}

	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Produto> cadastrar(@RequestBody @Validated Produto produto) {

		//for (int pos = 0; pos < produto.getTelefones().size(); pos++) {
		//	produto.getTelefones().get(pos).setUsuario(produto);
		//}

		//String senhacriptografada = new BCryptPasswordEncoder().encode(produto.getSenha());
		//usuario.setSenha(senhacriptografada);
		Produto produtoSalvo = produtoRepository.save(produto);

		//implementacaoUserDetailsSercice.insereAcessoPadrao(usuarioSalvo.getId());

		return new ResponseEntity<Produto>(produtoSalvo, HttpStatus.OK);

	}

	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Produto> atualizar(@RequestBody Produto produto) {

		/* outras rotinas antes de atualizar */

		//for (int pos = 0; pos < produto.getTelefones().size(); pos++) {
		//	usuario.getTelefones().get(pos).setUsuario(usuario);
		//}

		//Produto userTemporario = produtoRepository.findById(produto.getId()).get();

		//if (!userTemporario.getSenha().equals(usuario.getSenha())) { /* Senhas diferentes */
	//		String senhacriptografada = new BCryptPasswordEncoder().encode(produto.getSenha());
	//		usuario.setSenha(senhacriptografada);
		//}

		Produto produtoSalvo = produtoRepository.save(produto);

		return new ResponseEntity<Produto>(produtoSalvo, HttpStatus.OK);

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
	public String deleteTelefone(@PathVariable("id") Long id) {

		telefoneRepository.deleteById(id);

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

	@Override
	public void salvar(Produto objeto) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deletar(Produto objeto) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void salvarOuAtualizar(Produto objeto) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Produto merge(Produto objeto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Produto> lista() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Produto> lista(String ids) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Produto> lista(String campoBanco, String valorCampo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Produto> lista(String campoBanco, Long valorCampo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Produto> listaLikeExpression(String campoBanco, String valorCampo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Produto loadObjeto(Long codigo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int quantidadePagina() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Produto> consultaPaginada(String numeroPagina) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
	


