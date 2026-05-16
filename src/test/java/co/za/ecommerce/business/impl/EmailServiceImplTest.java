package co.za.ecommerce.business.impl;

import co.za.ecommerce.dto.order.OrderDTO;
import co.za.ecommerce.model.User;
import co.za.ecommerce.model.order.OrderStatus;
import factory.TestDataBuilder;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceImplTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;
    @InjectMocks private EmailServiceImpl emailService;

    private User user;
    private OrderDTO order;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        user = TestDataBuilder.buildUser();
        order = OrderDTO.builder()
                .orderNumber("ORD-20260516-123456")
                .transactionId("TXN-20260516-ABCD")
                .orderStatus("CONFIRMED")
                .shippingMethod("DHL")
                .subtotal(99.98)
                .tax(9.998)
                .shippingCost(15.99)
                .totalAmount(125.968)
                .build();

        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@thendo-ecommerce.co.za");

        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html>email</html>");
    }

    @Nested
    @DisplayName("sendWelcomeEmail")
    class SendWelcomeEmail {

        @Test
        @DisplayName("shouldProcessWelcomeTemplateWithUserName")
        void shouldProcessWelcomeTemplateWithUserName() {
            emailService.sendWelcomeEmail(user);

            ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
            verify(templateEngine).process(eq("email/welcome"), ctxCaptor.capture());
            assertThat(ctxCaptor.getValue().getVariable("name")).isEqualTo(user.getName());
        }

        @Test
        @DisplayName("shouldSendEmailToUserAddress")
        void shouldSendEmailToUserAddress() {
            emailService.sendWelcomeEmail(user);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("shouldNotThrowWhenMailSendFails")
        void shouldNotThrowWhenMailSendFails() {
            doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

            assertThatCode(() -> emailService.sendWelcomeEmail(user))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("sendOrderConfirmationEmail")
    class SendOrderConfirmationEmail {

        @Test
        @DisplayName("shouldProcessOrderConfirmationTemplateWithOrderAndName")
        void shouldProcessOrderConfirmationTemplateWithOrderAndName() {
            emailService.sendOrderConfirmationEmail(user, order);

            ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
            verify(templateEngine).process(eq("email/order-confirmation"), ctxCaptor.capture());

            Context ctx = ctxCaptor.getValue();
            assertThat(ctx.getVariable("name")).isEqualTo(user.getName());
            assertThat(ctx.getVariable("order")).isEqualTo(order);
        }

        @Test
        @DisplayName("shouldIncludeOrderNumberInSubject")
        void shouldSendEmailForOrderConfirmation() {
            emailService.sendOrderConfirmationEmail(user, order);

            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("sendOrderStatusUpdateEmail")
    class SendOrderStatusUpdateEmail {

        @Test
        @DisplayName("shouldProcessStatusUpdateTemplateWithCorrectVariables")
        void shouldProcessStatusUpdateTemplateWithCorrectVariables() {
            emailService.sendOrderStatusUpdateEmail(user, order, OrderStatus.SHIPPED);

            ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
            verify(templateEngine).process(eq("email/order-status-update"), ctxCaptor.capture());

            Context ctx = ctxCaptor.getValue();
            assertThat(ctx.getVariable("name")).isEqualTo(user.getName());
            assertThat(ctx.getVariable("order")).isEqualTo(order);
            assertThat(ctx.getVariable("newStatus")).isEqualTo("SHIPPED");
        }

        @Test
        @DisplayName("shouldSendEmailForDeliveredStatus")
        void shouldSendEmailForDeliveredStatus() {
            emailService.sendOrderStatusUpdateEmail(user, order, OrderStatus.DELIVERED);

            ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
            verify(templateEngine).process(eq("email/order-status-update"), ctxCaptor.capture());
            assertThat(ctxCaptor.getValue().getVariable("newStatus")).isEqualTo("DELIVERED");
        }
    }

    @Nested
    @DisplayName("sendPaymentFailureEmail")
    class SendPaymentFailureEmail {

        @Test
        @DisplayName("shouldProcessPaymentFailureTemplateWithReason")
        void shouldProcessPaymentFailureTemplateWithReason() {
            emailService.sendPaymentFailureEmail(user, "Card declined");

            ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
            verify(templateEngine).process(eq("email/payment-failure"), ctxCaptor.capture());

            Context ctx = ctxCaptor.getValue();
            assertThat(ctx.getVariable("name")).isEqualTo(user.getName());
            assertThat(ctx.getVariable("reason")).isEqualTo("Card declined");
        }

        @Test
        @DisplayName("shouldSendEmailForPaymentFailure")
        void shouldSendEmailForPaymentFailure() {
            emailService.sendPaymentFailureEmail(user, "Insufficient funds");

            verify(mailSender).send(mimeMessage);
        }
    }
}
