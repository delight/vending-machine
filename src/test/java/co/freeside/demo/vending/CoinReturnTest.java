package co.freeside.demo.vending;

import org.jmock.*;
import org.junit.*;
import static co.freeside.demo.vending.Coin.*;
import static co.freeside.demo.vending.Product.ChocolateSaltyBalls;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CoinReturnTest {

	private Mockery context = new Mockery();

	@Test
	public void machineReturnsCoin() {
		final HardwareDevice hardware = context.mock(HardwareDevice.class);
		VendingMachine machine = new VendingMachine(hardware);

		context.checking(new Expectations() {{
			oneOf(hardware).returnCoin(Penny);
		}});

		machine.insertCoin(Penny);
		machine.returnCoins();

		context.assertIsSatisfied();
	}

	@Test
    public void machineReturnsNoCoinsIfNoneAreInserted() {
		final HardwareDevice hardware = context.mock(HardwareDevice.class);
		VendingMachine machine = new VendingMachine(hardware);

		context.checking(new Expectations() {{
			never(hardware).returnCoin(with(any(Coin.class)));
		}});

		machine.returnCoins();

		context.assertIsSatisfied();
	}

    @Test
    public void machineDeductsReturnedCoinFromCredit() {
		final HardwareDevice hardware = context.mock(HardwareDevice.class);
		VendingMachine machine = new VendingMachine(hardware);

		context.checking(new Expectations() {{
			allowing(hardware).returnCoin(Penny);
		}});

		machine.insertCoin(Penny);
        machine.returnCoins();

		assertThat(machine.readCredit(), equalTo(0));
    }

    @Test
    public void machineReturnsEfficientChange() {
		final HardwareDevice hardware = context.mock(HardwareDevice.class);
		VendingMachine machine = new VendingMachine(hardware);
		machine.loadChange(Quarter, Dime, Nickel);

		context.checking(new Expectations() {{
			oneOf(hardware).returnCoin(Dime);
			oneOf(hardware).returnCoin(Nickel);
			exactly(2).of(hardware).returnCoin(Penny);
		}});

		for (int i = 0; i < 17; i++) machine.insertCoin(Penny);
        machine.returnCoins();

		context.assertIsSatisfied();
	}

    @Test
    public void machineReturnsAvailableChange() {
		final HardwareDevice hardware = context.mock(HardwareDevice.class);
		VendingMachine machine = new VendingMachine(hardware);

		context.checking(new Expectations() {{
			exactly(6).of(hardware).returnCoin(Penny);
		}});

		for (int i = 0; i < 6; i++) machine.insertCoin(Penny);
        machine.returnCoins();

		context.assertIsSatisfied();
    }

	@Test
	public void machineRetainsCreditIfItCannotMakeChange() {
		final HardwareDevice hardware = context.mock(HardwareDevice.class);
		VendingMachine machine = new VendingMachine(hardware);

		context.checking(new Expectations() {{
			oneOf(hardware).dispense(ChocolateSaltyBalls);
			never(hardware).returnCoin(with(any(Coin.class)));
		}});

		for (int i = 0; i < 4; i++) machine.insertCoin(Quarter);
		machine.purchase(ChocolateSaltyBalls);
		machine.returnCoins();

		context.assertIsSatisfied();
		assertThat(machine.readCredit(), equalTo(100 - ChocolateSaltyBalls.getPrice()));
	}

}
