package it.ids.hackathown.api.controller;

import it.ids.hackathown.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamenti")
@RequiredArgsConstructor
public class PagamentoController {

    private final PaymentService pagamentoService;

    public void erogaPremio() {
    }
}
