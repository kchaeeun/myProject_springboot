package com.example.myProject.controller;

import com.example.myProject.model.Board;
import com.example.myProject.repository.BoardRepository;
import com.example.myProject.service.BoardService;
import com.example.myProject.validator.BoardValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.Authenticator;
import java.util.List;

//경로 지정
@Controller
@RequestMapping("/board")
public class BoardController {
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardService boardService;


    //직접 만든 BoardValidator 사용하기 - spring 기동 시 instance가 담김
    @Autowired
    private BoardValidator boardValidator;

    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 2) Pageable pageable,
                       @RequestParam(required = false, defaultValue = "") String searchText) {   //@PageableDefault를 통해 기본 page값을 정할 수 있다.
        //Page<Board> boards = boardRepository.findAll(pageable);    //JPA에서 첫 페이지는 0이 기본값
        Page<Board> boards = boardRepository.findByTitleContainingOrContentContaining(searchText, searchText, pageable);    //JPA에서 첫 페이지는 0이 기본값

        int startPage = Math.max(1, boards.getPageable().getPageNumber() - 4);  //최소값은 0으로 설정, 뒤에 값은 최대값, 현 페이지의 넘버를 가져온다.
        int endPage = Math.min(boards.getTotalPages(), boards.getPageable().getPageNumber() + 4);   //(큰 값(전체 페이지 값), 작은 값)

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("boards", boards);
        return "board/list";
    }
    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id) {  //필요한 경우에만 Parameter를 전달하면 id값을 사용하겠다
        if(id == null) {
            model.addAttribute("board", new Board());
        } else {
            //board 조회하기 위해 repository에서 가져오기
            Board board = boardRepository.findById(id).orElse(null);    //orElse() -> 조회 값이 없을 경우에는 null
            model.addAttribute("board", board);

        }
        return "board/form";
    }
    @PostMapping("/form")
    //Validated는 Valid의 기능이 포함됨.
    public String postForm(@Valid Board board, BindingResult bindingResult, Authentication authentication) {
        boardValidator.validate(board, bindingResult);
        if (bindingResult.hasErrors()) {
            return "board/form";
        }
        //인증 정보
        //Authentication a = SecurityContextHolder.getContext().getAuthentication(); //전역변수 이용
        String username = authentication.getName();
        boardService.save(username, board);
//        boardRepository.save(board);
        return "redirect:/board/list"; ///list로의 redirect가 되면서 list에서 다시 한번 조회
    }
}
