package be.pxl.cargo.service;

import be.pxl.cargo.api.request.CreateCargoRequest;
import be.pxl.cargo.api.response.CargoStatistics;
import be.pxl.cargo.domain.Cargo;
import be.pxl.cargo.domain.CargoStatus;
import be.pxl.cargo.domain.Location;
import be.pxl.cargo.exceptions.NonUniqueCodeException;
import be.pxl.cargo.repository.CargoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CargoServiceTest {
    @Mock
    private CargoRepository cargoRepository;

    @InjectMocks
    private CargoService cargoService;

    //400 bad request indien de code niet uniek is
    @Test
    public void createCargo_shouldThrowException_when_code_is_not_unique() {
        CreateCargoRequest request = new CreateCargoRequest(
                "CARGO001",
                500,
                Location.WAREHOUSE_A,
                Location.CITY_B
        );

        Cargo existingCargo = new Cargo("CARGO001",500, Location.WAREHOUSE_A, Location.CITY_B);
        when(cargoRepository.findCargoByCode("CARGO001")).thenReturn(Optional.of(existingCargo));

        assertThrows(NonUniqueCodeException.class, () -> cargoService.createCargo(request));

        verify(cargoRepository, never()).save(any(Cargo.class));
    }

    @Test
    void createCargoSavesCargoWhenCodeIsUnique() {
        CreateCargoRequest request = new CreateCargoRequest(
                "CARGO002",
                750,
                Location.WAREHOUSE_A,
                Location.CITY_B
        );
        when(cargoRepository.findCargoByCode("CARGO002")).thenReturn(Optional.empty());

        cargoService.createCargo(request);

        verify(cargoRepository).save(any(Cargo.class));
    }

    @Test
    void getCargoStatisticsCalculatesOverview() {
        Cargo delivered = new Cargo("CARGO003", 900, Location.WAREHOUSE_A, Location.CITY_B);
        delivered.arrive(Location.CITY_B);
        Cargo created = new Cargo("CARGO004", 300, Location.WAREHOUSE_A, Location.CITY_C);
        Cargo moving = new Cargo("CARGO005", 600, Location.WAREHOUSE_A, Location.CITY_C);
        moving.setCargoStatus(CargoStatus.MOVING);
        when(cargoRepository.findAll()).thenReturn(List.of(delivered, created, moving));

        CargoStatistics statistics = cargoService.getCargoStatistics();

        assertThat(statistics.getHeaviestCargo()).isEqualTo("CARGO003");
        assertThat(statistics.getAverageCargoWeight()).isEqualTo(600.0);
        assertThat(statistics.getCountCargosAtWarehouseA()).isEqualTo(2);
        assertThat(statistics.getTotalWeightDeliveredAtCityB()).isEqualTo(900.0);
        assertThat(statistics.getStatusCount()).containsEntry(CargoStatus.DELIVERED, 1L);
    }
}
