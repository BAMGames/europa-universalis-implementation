set @idGame = 5;

delete from exotic_resources where id_establishment in (
    select es.id from establishment es 
    inner join counter counter on counter.id = es.id_counter
    inner join stack stack on stack.id = counter.id_stack 
    where stack.id_game = @idGame);
delete from establishment where id_counter in (
  select counter.id from counter counter
    inner join stack stack on stack.id = counter.id_stack
  where stack.id_game = @idGame);

delete from battle_counter where id_battle in (
  select id from battle where id_game = @idGame)
delete from battle where id_game = @idGame;
delete from siege_counter where id_siege in (
  select id from siege where id_game = @idGame);
delete from siege where id_game = @idGame;
delete from war_country where id_war in (
  select id from war where id_game = @idGame)
delete from war where id_game = @idGame;

delete from counter where id_stack in (
    select id from stack where id_game = @idGame);
delete from stack where id_game = @idGame;
delete from trade_fleet where id_game = @idGame;
delete from competition_round where id_competition in (
  select id from competition where id_game = @idGame);
delete from competition where id_game = @idGame;
delete from administrative_action where id_country in (
    select id from country where id_game = @idGame);
delete from economical_sheet where id_country in (
    select id from country where id_game = @idGame);
delete from country_order where id_game = @idGame;
delete from country where id_game = @idGame;

delete from c_message_global where id_c_room_global in (
  select id from c_room_global where id_game = @idGame);
delete from c_room_global where id_game = @idGame;
delete from c_message where id_country in (
  select id from country where id_game = @idGame);
delete from c_chat where id_c_room in (
  select id from c_room where id_game = @idGame);
delete from c_present where id_c_room in (
  select id from c_room where id_game = @idGame);
delete from c_room where id_game = @idGame;

delete from d_attribute where id_diff in (
  select id from d_diff where id_game = @idGame);
delete from d_diff where id_game = @idGame;

delete from game where id = @idGame;