package com.example.sauravbiswas.citruspay;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.citrus.sdk.Callback;
import com.citrus.sdk.CitrusClient;
import com.citrus.sdk.CitrusUser;
import com.citrus.sdk.Environment;
import com.citrus.sdk.TransactionResponse;
import com.citrus.sdk.classes.Amount;
import com.citrus.sdk.classes.CitrusConfig;
import com.citrus.sdk.classes.CitrusException;
import com.citrus.sdk.classes.LinkUserExtendedResponse;
import com.citrus.sdk.classes.LinkUserPasswordType;
import com.citrus.sdk.classes.LinkUserSignInType;
import com.citrus.sdk.classes.Month;
import com.citrus.sdk.classes.Year;
import com.citrus.sdk.payment.CardOption;
import com.citrus.sdk.payment.DebitCardOption;
import com.citrus.sdk.payment.PaymentType;
import com.citrus.sdk.response.CitrusError;
import com.citrus.sdk.response.CitrusResponse;
import com.citrus.sdk.ui.utils.CitrusFlowManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    CitrusClient citrusClient;
    Button btnRegister, btnOTP,btnDebit,btnLogout;
    EditText email, phoneNo, otp;
    TextView otpResponse;
    private LinkUserExtendedResponse linkUserExtended;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnOTP = (Button) findViewById(R.id.btnOTP);
        btnDebit = (Button) findViewById(R.id.btnDebit);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnRegister.setOnClickListener(this);
        btnOTP.setOnClickListener(this);
        btnDebit.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        email = (EditText) findViewById(R.id.edit_email);
        phoneNo = (EditText) findViewById(R.id.edit_phone);
        otp = (EditText) findViewById(R.id.edit_otp);
        otpResponse = (TextView)findViewById(R.id.otpResponse);
        citrusClient = CitrusClient.getInstance(getApplicationContext());
        citrusClient.enableLog(true);
        citrusClient.init(
                "test-signup",
                "c78ec84e389814a05d3ae46546d16d2e",
                "test-signin",
                "52f7e15efd4208cf5345dd554443fd99",
                "testing",
                Environment.SANDBOX );
        citrusClient.isUserSignedIn(new com.citrus.sdk.Callback<Boolean>() {
            @Override
            public void success(Boolean loggedIn) {
                Toast.makeText(getApplicationContext(), "User is signed In", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error(CitrusError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnRegister:
                String emailString = email.getText().toString();
                String phoneString = phoneNo.getText().toString();
                //Toast.makeText(getApplicationContext(),"Clicked",Toast.LENGTH_SHORT).show();
                LinkUser(emailString, phoneString, citrusClient);
                break;
            case R.id.btnOTP:
                linkUserExtendedSignin();
                break;
            case R.id.btnDebit:
                try {
                    debitCardPayment();
                } catch (CitrusException e) {
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnLogout:
                citrusClient.signOut(new Callback<CitrusResponse>() {

                    @Override
                    public void success(CitrusResponse citrusResponse) {
                        Toast.makeText(getApplicationContext(),citrusResponse.getMessage(),Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void error(CitrusError error) {
                        Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }

    private void debitCardPayment() throws CitrusException {

        String BILL_URL = "https://fase.herokuapp.com/billgenerator";
        CitrusClient citrusClient = CitrusClient.getInstance(getApplicationContext()); // Activity Context
        // No need to call init on CitrusClient if already done.

        DebitCardOption debitCardOption = new DebitCardOption("SIPRA BISWAS", "5105570920100109", "488", Month.getMonth("10"), Year.getYear("19"));

        Amount amount = new Amount("5");
        // Init PaymentType
        PaymentType.PGPayment pgPayment = new PaymentType.PGPayment(amount, BILL_URL, debitCardOption, new CitrusUser("developercitrus@gmail.com","8017504268"));

        citrusClient.simpliPay(pgPayment, new Callback<TransactionResponse>() {

            @Override
            public void success(TransactionResponse transactionResponse) {
                Toast.makeText(getApplicationContext(),transactionResponse.getMessage(),Toast.LENGTH_LONG).show();
                otpResponse.setText(transactionResponse.getMessage());
            }

            @Override
            public void error(CitrusError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                otpResponse.setText(error.getMessage());
            }
        });
    }

    private void LinkUser(String emailId, String phoneNo, CitrusClient citrusClient) {
        citrusClient.linkUserExtended(emailId, phoneNo, new Callback<LinkUserExtendedResponse>() {
            @Override
            public void success(LinkUserExtendedResponse linkUserExtendedResponse) {
                // User Linked!
                linkUserExtended = linkUserExtendedResponse;
                LinkUserSignInType linkUserSignInType = linkUserExtendedResponse.getLinkUserSignInType();
                String linkUserMessage = linkUserExtendedResponse.getLinkUserMessage();
                Toast.makeText(getApplicationContext(), linkUserMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error(CitrusError error) {
                // Error case
                String errorMessasge = error.getMessage();
                Toast.makeText(getApplicationContext(),errorMessasge,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void linkUserExtendedSignin() {
        String linkUserPassword = null;
        String otpPassword = otp.getText().toString();
        LinkUserPasswordType linkUserPasswordType = LinkUserPasswordType.None;
        if(otpPassword.length() > 0) {
            linkUserPasswordType = LinkUserPasswordType.Otp;
            linkUserPassword = otpPassword;
        }
        citrusClient.linkUserExtendedSignIn(linkUserExtended,linkUserPasswordType,linkUserPassword, new Callback<CitrusResponse>(){
            @Override
            public void success(CitrusResponse citrusResponse) {
                // User Signed In!
                Toast.makeText(getApplicationContext(),citrusResponse.getMessage(),Toast.LENGTH_SHORT).show();
                otpResponse.setText(citrusResponse.getMessage());
            }
            @Override
            public void error(CitrusError error) {
                // Error case
                String errorMessasge = error.getMessage();
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                otpResponse.setText(error.getMessage());
            }
        });
    }
}
