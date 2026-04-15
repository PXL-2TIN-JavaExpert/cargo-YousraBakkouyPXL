package be.pxl.cargo.api;

import be.pxl.cargo.api.request.CreateCargoRequest;
import be.pxl.cargo.domain.Cargo;
import be.pxl.cargo.domain.Location;
import be.pxl.cargo.exceptions.NonUniqueCodeException;
import be.pxl.cargo.service.CargoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CargoController.class)
public class CargoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CargoService cargoService;

    //converteert data van incompatible type systems naar een Java object
    //vaak JSON strings naar java objecten
    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
    Hoe maak je een test
    1 Maak testdata(request)
    2. Simuleer een HTTP call met mockMVC
    3. Controleer de reponse status
    4. Verifier dat de juiste service methode werd gebruikt met verify
     */

    @Test
    //we schrijven throws Exception omdat mockmcv.perform en objectMapper.writeValueasstring exceptions kunnen gooien
    //door throws exception te gebruiken hoeven we geen try-catch te schrijven in de test.
    public void addCargo_shouldReturnCreated() throws Exception {
        CreateCargoRequest request = new CreateCargoRequest(
                "CARGO001",
                500,
                Location.WAREHOUSE_A,
                Location.CITY_B
        );

        //.perform simuleert een POST request naar /cargos met een gesimuleerde JSON-body
        mockMvc.perform(post("/cargos")//DIT IS een post endpoint
                        .contentType(MediaType.APPLICATION_JSON)//Geeft aan dat je JSON verstuurt
                        .content(objectMapper.writeValueAsString(request)))//zet je object om naar JSON
                .andExpect(status().isCreated());//checkt of de response 201 is

        //create cargo is een void method
        //Hij checked of createCargo effectief werd aangeroepen
        verify(cargoService).createCargo(any(CreateCargoRequest.class));
        //verify checked of de service werd aangeroepen
        //ArgumentMatchers.any() -> //maakt niet uit welke exacte input,zolang de methode wordt aangeroepen
    }

    //400 bad request indien het gewicht onder 100
    @Test
    public void addCargo_shouldReturn400_when_weight_is_below_100() throws Exception {
        CreateCargoRequest request = new CreateCargoRequest(
                "CARGO001",
                95,
                Location.WAREHOUSE_A,
                Location.CITY_B
        );

        mockMvc.perform(post("/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        //dit is hoe je checked als iets nooit wordt aangeroepen
        //Verify wordt niet vaak gebruikt in de boek is gewoon een nice extra voor de zekerheid
        verify(cargoService, never())
                .createCargo(any(CreateCargoRequest.class));

    }

    //400 bad request indien de code ontbreekt
    @Test
    public void addCargo_shouldReturn400_when_there_is_no_code() throws Exception {
        CreateCargoRequest request = new CreateCargoRequest(
                "",
                500,
                Location.WAREHOUSE_A,
                Location.CITY_B
        );

        mockMvc.perform(post("/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()
                );

//        Mockito.verify(cargoService, Mockito.never())
//                .createCargo(ArgumentMatchers.any(CreateCargoRequest.class));
        //met static imports hoef je geen Mockito erbij op te roepen
//        verify(cargoService, never())
//                .createCargo(ArgumentMatchers.any(CreateCargoRequest.class));
        //zelfde geldt voor ArgumentMatchers je kan gewoon any() schrijven
        verify(cargoService, never())
                .createCargo(any(CreateCargoRequest.class));
    }

}
