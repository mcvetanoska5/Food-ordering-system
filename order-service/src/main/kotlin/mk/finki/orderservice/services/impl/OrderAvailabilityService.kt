package mk.finki.orderservice.services.impl

import mk.finki.orderservice.acl.translator.UpdateMenuItemAvailabilityCommand
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderAvailabilityService {
    private val unavailableMenuItems = ConcurrentHashMap.newKeySet<UUID>()

    fun handle(command: UpdateMenuItemAvailabilityCommand) {
        if (command.available) {
            unavailableMenuItems.remove(command.menuItemId.value)
        } else {
            unavailableMenuItems.add(command.menuItemId.value)
        }
    }

    fun isAvailable(menuItemId: UUID): Boolean = !unavailableMenuItems.contains(menuItemId)
}
