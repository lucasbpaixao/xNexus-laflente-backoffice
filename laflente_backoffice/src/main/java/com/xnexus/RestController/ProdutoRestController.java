package com.xnexus.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;


import com.xnexus.model.Imagem;
import com.xnexus.model.PalavraChave;
import com.xnexus.model.PerguntaResposta;
import com.xnexus.model.Produto;
import com.xnexus.repository.ImagemRepository;
import com.xnexus.repository.PalavraRepository;
import com.xnexus.repository.PerguntaRespostaRepository;
import com.xnexus.repository.ProdutoRepository;

@RestController
@RequestMapping("/produto")
public class ProdutoRestController {

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private ImagemRepository imagemRepository;

	@Autowired
	private PalavraRepository palavraRepository;
	
	@Autowired
	private PerguntaRespostaRepository perguntaRespostaRepository;

	@PostMapping
	@Transactional
	public ResponseEntity<Produto> cadastrarProduto(@RequestBody @Valid Produto produto,
			UriComponentsBuilder uriBuilder) {
		produto.setStatus("ATIVO");
		produtoRepository.save(produto);

		URI uri = uriBuilder.path("/produto/{codigo}").buildAndExpand(produto.getCodigo()).toUri();

		return ResponseEntity.created(uri).body(produto);
	}

	@PostMapping("/palavrasChave/{codigoProduto}")
	@Transactional
	public ResponseEntity<PalavraChave> cadastrarPalavra(@RequestBody @Valid PalavraChave palavraChave,
			@PathVariable Long codigoProduto, UriComponentsBuilder uriBuilder) {

		Optional<PalavraChave> optionalPalavra = palavraRepository.findById(palavraChave.getId());
		Optional<Produto> optional = produtoRepository.findById(codigoProduto);

		if (!optionalPalavra.isPresent()) {
			if (optional.isPresent()) {
				palavraRepository.save(palavraChave);
				Produto produto = optional.get();
				produto.addPalavraChave(palavraChave);

				URI uri = uriBuilder.path("/produto/palavrasChave/{codigo}")
						.buildAndExpand(palavraChave.getPalavraChave(), palavraChave.getCodigoProduto()).toUri();

				return ResponseEntity.created(uri).body(palavraChave);
			}
		}
		return ResponseEntity.created(null).body(null);
	}
	
	@PostMapping("/perguntasRespostas/{codigoProduto}")
	@Transactional
	public ResponseEntity<PerguntaResposta> cadastrarPerguntaResposta(@RequestBody @Valid PerguntaResposta perguntaResposta,
			@PathVariable Long codigoProduto, UriComponentsBuilder uriBuilder) {

		Optional<PerguntaResposta> optionalPerguntaResposta = perguntaRespostaRepository.findById(perguntaResposta.getId());
		Optional<Produto> optional = produtoRepository.findById(codigoProduto);

		if (!optionalPerguntaResposta.isPresent()) {
			if (optional.isPresent()) {
				perguntaRespostaRepository.save(perguntaResposta);
				Produto produto = optional.get();
				produto.addPerguntaResposta(perguntaResposta);

				URI uri = uriBuilder.path("/produto/perguntasRespostas/{codigo}")
						.buildAndExpand(perguntaResposta.getPergunta(), perguntaResposta.getResposta(), perguntaResposta.getCodigoProduto()).toUri();

				return ResponseEntity.created(uri).body(perguntaResposta);
			}
		}
		return ResponseEntity.created(null).body(null);
	}

	@PutMapping(value = "/imagem/{codigo}-{nome}-{numeroImagens}-{extensao}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Transactional
	public void uploadImagem(@RequestParam MultipartFile imagem, @PathVariable Long codigo, @PathVariable String nome,
			@PathVariable int numeroImagens, @PathVariable String extensao, UriComponentsBuilder uriBuilder) {

		try {
			Optional<Produto> optional = produtoRepository.findById(codigo);

			if (optional.isPresent()) {

				String nomeArquivo = codigo + "_" + nome + "_" + imagem.getName() + "_img_" + numeroImagens;

				MessageDigest md = MessageDigest.getInstance("MD5");

				String md5 = md.digest(nomeArquivo.getBytes()).toString();
				Imagem imagemObj = new Imagem(md5, imagem.getBytes(), codigo, extensao);

				Produto produtos = optional.get();

				imagemRepository.save(imagemObj);

				produtos.addImagem(imagemObj);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@GetMapping
	public ResponseEntity<List<Produto>> listarProdutos() {
		List<Produto> produtos = produtoRepository.findAll();

		return ResponseEntity.ok(produtos);
	}

	@PutMapping("/{codigo}")
	@Transactional
	public ResponseEntity<Produto> alterarProduto(@PathVariable Long codigo, @RequestBody @Valid Produto produto) {

		Optional<Produto> optional = produtoRepository.findById(codigo);

		if (optional.isPresent()) {
			Produto produtos = optional.get();

			produtos.setNome(produto.getNome());
			produtos.setPreco(produto.getPreco());
			produtos.setQuantidade(produto.getQuantidade());
			// produtos.setPalavraChave(produto.getPalavraChave());
			produtos.setDescricaoCurta(produto.getDescricaoCurta());
			produtos.setDescricaoDetalhada(produto.getDescricaoDetalhada());
			// produtos.setImagem(produto.getImagem());

			return ResponseEntity.ok(produtos);
		}

		return ResponseEntity.notFound().build();
	}
	
	@PutMapping("/quantidade/{codigo}")
	@Transactional
	public ResponseEntity<Produto> alterarQuantidade(@PathVariable Long codigo, @RequestBody int quantidade) {

		Optional<Produto> optional = produtoRepository.findById(codigo);

		if (optional.isPresent()) {
			Produto produtos = optional.get();

			produtos.setQuantidade(quantidade);

			return ResponseEntity.ok(produtos);
		}

		return ResponseEntity.notFound().build();
	}

	@GetMapping("/{codigo}")
	public ResponseEntity<Produto> buscarProduto(@PathVariable Long codigo) {

		Optional<Produto> optional = produtoRepository.findById(codigo);

		if (optional.isPresent()) {
			Produto produtos = optional.get();
			produtos.setImagem(imagemRepository.findByCodigoProduto(codigo));

			return ResponseEntity.ok(produtos);
		}

		return ResponseEntity.notFound().build();
	}

	@GetMapping("/consultaNome/{nome}")
	@Transactional
	public ResponseEntity<Produto> buscarProdutoNome(@PathVariable String nome) {
		Optional<Produto> optional = produtoRepository.findByNome(nome);

		if (optional.isPresent()) {
			Produto produtos = optional.get();

			return ResponseEntity.ok(produtos);
		}

		return ResponseEntity.notFound().build();
	}

	@GetMapping("/consultaStatus/{status}")
	@Transactional
	public ResponseEntity<List<Produto>> buscarProdutoStatus(@PathVariable String status) {

		List<Produto> produtos = produtoRepository.findByStatus(status);
		return ResponseEntity.ok(produtos);

	}

	@PatchMapping("/{codigo}")
	@Transactional
	public ResponseEntity<?> removerProduto(@PathVariable Long codigo, @RequestBody String status) {

		Optional<Produto> optional = produtoRepository.findById(codigo);

		if (optional.isPresent()) {
			Produto produtos = optional.get();

			produtos.setStatus(status);
			return ResponseEntity.ok(produtos);
		}

		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<?> removerImagem(@PathVariable Long id) {

		Optional<Imagem> optional = imagemRepository.findById(id);

		if (optional.isPresent()) {
			imagemRepository.deleteById(id);

			return ResponseEntity.ok().build();
		}

		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/palavrasChave/{id}")
	@Transactional
	public ResponseEntity<?> removerPalavraChave(@PathVariable Integer id) {

		Optional<PalavraChave> optional = palavraRepository.findById(id);

		if (optional.isPresent()) {
			palavraRepository.deleteById(id);

			return ResponseEntity.ok().build();
		}

		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/perguntasRespostas/{id}")
	@Transactional
	public ResponseEntity<?> removerPerguntaResposta(@PathVariable Integer id) {

		Optional<PerguntaResposta> optional = perguntaRespostaRepository.findById(id);

		if (optional.isPresent()) {
			perguntaRespostaRepository.deleteById(id);

			return ResponseEntity.ok().build();
		}

		return ResponseEntity.notFound().build();
	}

}
