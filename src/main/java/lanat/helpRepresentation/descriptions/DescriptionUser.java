package lanat.helpRepresentation.descriptions;

import lanat.utils.CommandUser;
import lanat.utils.NamedWithDescription;

/**
 * The type of the user that is requesting to parse a description.
 * It must belong to a command and have a name and a description.
 */
public interface DescriptionUser extends CommandUser, NamedWithDescription { }