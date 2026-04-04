package org.example.nextstepbackend.controller;

import jakarta.validation.Valid;
import org.example.nextstepbackend.controller.base.BaseController;
import org.example.nextstepbackend.dto.request.ChecklistRequest;
import org.example.nextstepbackend.dto.request.ChecklistResponse;
import org.example.nextstepbackend.dto.response.common.ApiResponse;
import org.example.nextstepbackend.enums.MessageConst;
import org.example.nextstepbackend.services.checklist.ChecklistService;
import org.example.nextstepbackend.utils.ApiResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checklists")
public class ChecklistController extends BaseController {

    private final ChecklistService checklistService;

    public ChecklistController(ApiResponseUtil responseUtil, ChecklistService checklistService) {
        super(responseUtil);
        this.checklistService = checklistService;
    }

    @PostMapping("/{cardId}")
    public ResponseEntity<ApiResponse<ChecklistResponse>> createChecklists(@PathVariable Integer cardId, @Valid @RequestBody ChecklistRequest request){
        ChecklistResponse response =  checklistService.createChecklist(cardId, request);
        return ResponseEntity.ok(success(MessageConst.CHECKLIST_CREATE_SUCCESS,response));
    }
}
