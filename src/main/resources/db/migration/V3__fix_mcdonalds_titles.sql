-- Seed data had "McDonald s" (space instead of apostrophe); it showed in the game.
update items set title = 'McDonald''s'        where title = 'McDonald s';
update items set title = 'McDonald''s Big Mac' where title = 'McDonald s Big Mac';
