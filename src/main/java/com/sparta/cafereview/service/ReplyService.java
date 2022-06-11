package com.sparta.cafereview.service;

import com.sparta.cafereview.model.Reply;
import com.sparta.cafereview.repository.ReplyRepository;
import com.sparta.cafereview.requestdto.ReplyRequestDto;
import com.sparta.cafereview.responsedto.ReplyResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReplyService {
    private final ReplyRepository replyRepository;

    // 댓글 조회
    public List<ReplyResponseDto> getReply(Long cafeId) {
        List<Reply> replies = replyRepository.findAllByCafeId(cafeId);
        List<ReplyResponseDto> list = replies.stream().map(ReplyResponseDto::new).collect(Collectors.toList());
        return list;
    }
    // 댓글 작성
    public boolean createReply(ReplyRequestDto requestDto, Long userId, Long cafeId){
        replyValidate(requestDto);
        Reply reply = new Reply(requestDto, userId, cafeId);
        replyRepository.save(reply);
        log.info("replyid : {} 댓글 작성 완료하였습니다.", reply.getId());
        return true;
    }

    private void replyValidate(ReplyRequestDto requestDto){
        String replyCheck = requestDto.getReply();
        if (replyCheck.contains("script") || replyCheck.contains("<") || replyCheck.contains(">")){
            log.warn("xss 공격은 불가합니다.");
            throw new RuntimeException("xss 공격은 불가합니다.");
        }
    }

    // 댓글 업데이트
    public boolean update(Long replyid, ReplyRequestDto requestDto, String nickname, Long userId, Long cafeId){
        replyValidate(requestDto);
        Reply reply = replyRepository.findById(replyid).orElseThrow(
                () -> new IllegalArgumentException("댓글이 존재하지 않습니다.")
        );
        Long writerId = reply.getUserId();
        if(Objects.equals(writerId, userId)){
            reply.update(requestDto);
            replyRepository.save(reply);
            log.info("replyid : {} 댓글 수정 완료하였습니다.", reply.getId());
            return true;
        }
        log.info("작성한 유저가 아닙니다.");
        return false;
    }
    //댓글 삭제
    public boolean deleteReply(Long replyId, Long userId, Long cafeId){
        Long writeId = replyRepository.findById(replyId).orElseThrow(
                () -> new IllegalArgumentException("댓글이 존재하지 않습니다.")
        ).getUserId();
        if(Objects.equals(writeId, userId)){
            replyRepository.deleteById(replyId);
            log.info("replyid : {} 댓글 삭제 완료하였습니다.", replyId);
            return true;
        }
        log.info("작성한 유저가 아닙니다.");
        return false;
    }
}
