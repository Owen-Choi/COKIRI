package f3f.dev1.domain.trade.dto;

import f3f.dev1.domain.member.model.Member;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.post.model.Post;
import f3f.dev1.domain.trade.model.Trade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TradeDTO {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateTradeDto {
        private Long sellerId;

        private Long buyerId;

        private Long postId;

        public Trade toEntity(Member seller, Member buyer, Post post) {
            return Trade.builder()
                    .seller(seller)
                    .buyer(buyer)
                    .post(post)
                    .tradeStatus(TradeStatus.TRADABLE).build();
        }

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateTradeDto {
        private Long tradeId;

        private Long userId;

        private TradeStatus tradeStatus;
    }


    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradeInfoDto{
        private String sellerNickname;

        private String buyerNickname;

        private TradeStatus tradeStatus;
    }
}