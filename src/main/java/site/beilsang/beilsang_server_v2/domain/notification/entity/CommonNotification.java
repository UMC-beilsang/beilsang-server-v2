package site.beilsang.beilsang_server_v2.domain.notification.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@DiscriminatorValue("D")
public class CommonNotification extends AppNotification{
}

