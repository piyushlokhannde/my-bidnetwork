package bid.client.blockchain;


import org.hyperledger.fabric.sdk.User;
import org.junit.Test;

import static bid.client.blockchain.Participant.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;


public class TestAppUser {


  @Test
  public void create_AppUser_Object_to_test_name_msp() {
      User user  =  new AppUser(GOV);
      assertThat(user.getName(), equalTo(GOV.name));
      assertThat(user.getMspId(), equalTo(GOV.mspId));
  }

  @Test
  public void create_AppUser_To_test_Roles_Account_Affiliation() {
      User user  =  new AppUser(GOV);
      assertThat(user.getAccount(), equalTo(""));
      assertThat(user.getAffiliation(), equalTo(""));
      assertThat(user.getRoles().size(), equalTo(0));

  }

  @Test
  public void create_AppUser_To_test_certificate() {
      User user  =  new AppUser(GOV);
      assertThat(user.getEnrollment().getCert(), is(notNullValue()));
  }


    @Test
    public void create_AppUser_To_test_privatekey() {
        User user  =  new AppUser(GOV);
        assertThat(user.getEnrollment().getKey(), is(notNullValue()));
    }


}
